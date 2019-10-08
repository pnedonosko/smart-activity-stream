(function($, cCometD) {
  "use strict";

  const relevantText = "Click to mark as irrelevant";
  const irrelevantText = "Click to mark as relevant";
  const neutralText = "Click to mark as neutral";
  
  const PROFILE_ATTENTION_TIME = 2000;
  const QUICK_ATTENTION_TIME = 5000;
  const BRIEF_ATTENTION_TIME = 10000;
  const FULL_ATTENTION_TIME = 20000;
  const PAGE_LOAD_ATTENTION_TIME = BRIEF_ATTENTION_TIME; // was 5000
  
  const SCROLL_FORWARD = "forward";
  const SCROLL_BACK = "back";
  const LOOK_TOP = "top";
  const LOOK_BOTTOM = "bottom";
  const LOOK_FULL = "full";
  const LOOK_OVERSIZE = "oversize";
  
  const SHOW_ALL = "oversize";
  const SHOW_CONTENT = "oversize";
  const SHOW_CONVO = "oversize";
  
  const $window = $(window);
  const $document = $(document);
  
  // User
  let currentUserId;
  
  // CometD transport bus
  let cometd, cometdContext;
  
  if (eXo) {
    if (!eXo.smartactivity) {
      eXo.smartactivity = {
        debug : false
      };
    }
  }
  eXo.smartactivity.debug = false; // for dev purpose
  
  // Polyfill String.startsWith()
  if (!String.prototype.startsWith) {
    Object.defineProperty(String.prototype, "startsWith", {
      value: function(search, rawPos) {
        let pos = rawPos > 0 ? rawPos|0 : 0;
        return this.substring(pos, pos + search.length) === search;
      }
    });
  }
  
  /** For debug logging. */
  function log(msg, err) {
    const logPrefix = "[smartactivity] ";
    if (typeof console != "undefined" && typeof console.log != "undefined") {
      let isoTime = " -- " + new Date().toISOString();
      let msgLine = msg;
      if (err) {
        msgLine += ". Error: ";
        if (err.name || err.message) {
          if (err.name) {
            msgLine += "[" + err.name + "] ";
          }
          if (err.message) {
            msgLine += err.message;
          }
        } else {
          msgLine += (typeof err === "string" ? err : JSON.stringify(err)
              + (err.toString && typeof err.toString === "function" ? "; " + err.toString() : ""));
        }

        console.log(logPrefix + msgLine + isoTime);
        if (typeof err.stack != "undefined") {
          console.log(err.stack);
        }
      } else {
        if (err !== null && typeof err !== "undefined") {
          msgLine += ". Error: '" + err + "'";
        }
        console.log(logPrefix + msgLine + isoTime);
      }
    }
  }
  
  function debug(msg, err) {
    if (eXo.smartactivity.debug) {
      log(msg, err);
    }
  }

  function tryParseJson(message) {
    const src = message.data ? message.data : (message.error ? message.error : message.failure);
    if (src) {
      try {
        if (typeof src === "string" && (src.startsWith("{") || src.startsWith("["))) {
          return JSON.parse(src);
        }
      } catch (e) {
        log("Error parsing '" + src + "' as JSON: " + e, e);
      }
    }
    return src;
  }
  
  /**
   * Subscribes on a userfocus updates using cometd.
   */
  // TODO not used
  function subscribeUserFocus(userId) {
    let subscription = cometd.subscribe("/eXo/Application/SmartActivity/userfocus/" + userId, function(message) {
      // Channel message handler
      let result = tryParseJson(message);
      // TODO handle other pages updates?
    }, cometdContext, function(subscribeReply) {
      // Subscription status callback
      if (subscribeReply.successful) {
        // The server successfully subscribed this client to the channel.
        debug("User focus channel subscribed successfully: " + JSON.stringify(subscribeReply));
        // TODO init something?
      } else {
        var err = subscribeReply.error ? subscribeReply.error : (subscribeReply.failure ? subscribeReply.failure.reason
            : "Undefined");
        log("User focus channel subscription failed for " + userId, err);
      }
    });
  }
  
  function saveFocus(data) {
    const deferred = $.Deferred();
    cometd.publish("/eXo/Application/SmartActivity/userfocus/" + currentUserId, data, cometdContext, function(publishReply) {
      // Publication status callback
      if (publishReply.successful) {
        deferred.resolve(publishReply);
        // The server successfully subscribed this client to the channel.
        debug("<< " + data.activityId + " User focus published successfully: " + JSON.stringify(publishReply));
      } else {
        const err = publishReply.error ? publishReply.error : (publishReply.failure ? publishReply.failure.reason : "Undefined");
        deferred.reject(err);
        log("User focus publication failed for " + currentUserId, err);
      }
    });
    return deferred.promise();
  }
  
  // Finds out page base url
  let prefixUrl = function() {
    let theHostName = window.location.hostname;
    if (window.location.port) {
      theHostName += ":" + window.location.port;
    }
    return window.location.protocol + "//" + theHostName;
  }();
  
  function postRelevance(relevance) {
    return $.ajax({
      url : prefixUrl + "/portal/rest/smartactivity/relevancy/" + relevance.userId + "/" + relevance.activityId,
      type : "post",
      contentType : "application/json",
      data : JSON.stringify(relevance)
    });
  };

  function getRelevance(userId, activityId) {
    return $.ajax({
      url : prefixUrl + "/portal/rest/smartactivity/relevancy/" + userId + "/" + activityId,
      type : "get"
    });
  };

  // Save information about relevance of the activity to the server
  function saveRelevance(activityId, relevant) {
    // The object of relevance to be sent to the server
    const relevance = {
      "userId" : eXo.env.portal.userName,
      "activityId" : activityId
    };
    if (relevant != null) {
      relevance.relevant = relevant;
    }
    postRelevance(relevance);
  }
  
  // Adds onClick listener to the elements
  function addRelevanceOnClickListener(elements) {
    $(elements).click(function() {
      const $elem = $(this);
      if (!$elem.attr("onClick")) {
        // The link contains activityId
        let activityId = $elem.parents(".activityStream").attr("id");
        activityId = activityId ? activityId.replace("activityContainer", "") : "";
        if ($elem.hasClass("relevance-default")) {
          debug("Action: relevant | ID: " + activityId);
          saveRelevance(activityId, true);
          $elem.removeClass("relevance-default");
          $elem.toggleClass("relevance-relevant");
          $elem.toggleClass("uiIconBlue");
          $elem.closest("a.relevance-tooltip").attr("data-original-title", relevantText);
        } else if ($elem.hasClass("relevance-relevant")) {
          debug("Action: irrelevant | ID: " + activityId);
          saveRelevance(activityId, false);
          $elem.removeClass("relevance-relevant");
          $elem.toggleClass("relevance-irrelevant");
          $elem.closest("a.relevance-tooltip").attr("data-original-title", neutralText);
        } else {
          debug("Action: default relevance | ID: " + activityId);
          saveRelevance(activityId, null);
          $elem.removeClass("relevance-irrelevant");
          $elem.toggleClass("relevance-default");
          $elem.removeClass("uiIconBlue");
          $elem.closest("a.relevance-tooltip").attr("data-original-title", irrelevantText);
        }
      }
    });
  }

  function getActivityId($activityStream) {
    const activityId = $activityStream.attr("id");
    return activityId ? activityId.replace("activityContainer", "") : "";
  }
  
  // Updates state of the icons. Accepts any parent div of an icon element
  function updateStateOfIcons($activities) {
    // Iterates through each activity block and inserts the relevance icon
    $activities.find(".boxContainer .actionBar > .statusAction.pull-right").each(function() {
      const $elem = $(this);
      if ($elem.find(".relevance").length == 0) {
        // The link contains activityId
        const activityId = getActivityId($elem.parents(".activityStream"));
        const userId = eXo.env.portal.userName;
        getRelevance(userId, activityId).done(function(data) {
          // If server responded with relevance
          if (data.relevant != null) {
            if (data.relevant) {
              $elem.prepend(getRelevantIcon());
            } else {
              $elem.prepend(getIrrelevantIcon());
            }
          } else {
            $elem.prepend(getDefaultIcon());
          }
          // Add onClick listener to new icon
          addRelevanceOnClickListener($elem.find(".relevance"));
          // Call tooltip handler
          $elem.find("a.relevance-tooltip").tooltip();
        }).fail(function(jqXHR) {
          // If server responded with error
          if (jqXHR.status == 404) {
            // If user hasn't checked relevance for the activity: add default icon
            $elem.prepend(getDefaultIcon());
            // Add onClickListener to new icon
            addRelevanceOnClickListener($elem.find(".relevance"));
          } else {
            log("Smart Activity: Error status: " + jqXHR.status + ", text: " + jqXHR.statusText);
          }
          // Call tooltip handler
          $elem.find("a.relevance-tooltip").tooltip();
        });
      } // Otherwise, if there is already icon move to the next block
    });
  }

  const focused = new Map();
  
  function Focus(activityId, initTime) {
    const self = this;
    
    let tracker;
    let updateTime = null;
    let contentTime = 0;
    let contentSize = 1;
    let convoTime = 0;
    let convoSize = 1;
    let contentShownMs = 0;
    let convoShownMs = 0;
    
    this.userId = currentUserId;
    this.activityId = activityId;
    this.initTime = initTime; // TODO need it?
    this.startTime = null;
    this.stopTime = null;
    this.totalShown = 0;
    this.contentShown = 0;
    this.convoShown = 0;
    this.contentHits = null;
    this.convoHits = null;
    this.profileHits = null;
    this.appHits = null;
    this.linkHits = null;
    
    const fullAttentionTime = function() {
      return FULL_ATTENTION_TIME * (contentSize + convoSize) / 2;
    };
    
    const track = function(timeout) {
      if (!tracker) {
        if (!timeout) {
          timeout = fullAttentionTime();
        }
        tracker = setTimeout(function() {
          const timeNow = new Date().getTime();
          let timeToWait;
          if (updateTime && (timeToWait = timeNow - updateTime) >= fullAttentionTime()) {
            self.fixate();  
          } else {
            tracker = null;
            track(timeToWait); // track until reach attention time
          }
        }, timeout);
      }
    };
    
    const fixateContentTime = function(timeNow) {
      if (contentTime > 0) {
        const timeShown = timeNow - contentTime;
        if (timeShown >= QUICK_ATTENTION_TIME * contentSize) {
          contentShownMs += Math.round(timeShown / contentSize);
          debug("<<< " + activityId + " fixateContentTime: " + self.contentShown + " (+" + timeShown + ") size: " + contentSize);
        }
        contentTime = 0;
      }
    }
    
    const fixateConvoTime = function(timeNow) {
      if (convoTime > 0) {
        const timeShown = timeNow - convoTime;
        if (timeShown >= QUICK_ATTENTION_TIME * convoSize) {
          convoShownMs += Math.round(timeShown / convoSize);
          debug("<<< " + activityId + " fixateConvoTime: " + self.convoShown + " (+" + timeShown + ") size: " + convoSize);
        }
        convoTime = 0;
      }
    };
    
    this.contentShow = function(focusSize) {
      track();
      const timeNow = new Date().getTime();
      //console.log(">> " + activityId + " contentShown: " + this.convoShown + " >> " + timeNow);
      if (contentTime == 0) {
        contentTime = timeNow; 
      }
      if (focusSize > 0) {
        contentSize = focusSize;
      }
      updateTime = timeNow;
      if (!this.startTime) {
        this.startTime = timeNow; 
      }
      return this;
    };
    
    this.contentHide = function() {
      track();
      const timeNow = new Date().getTime();
      //console.log("<< " + activityId + " contentHidden: " + this.convoShown + " << " + timeNow);
      fixateContentTime(timeNow);
      updateTime = timeNow;        
      return this;
    };
    
    this.convoShow = function(focusSize) {
      track();
      const timeNow = new Date().getTime();
      //console.log(">> " + activityId + " convoShown: " + this.convoShown + " >> " + timeNow);
      if (convoTime == 0) {
        convoTime = timeNow; 
      }
      if (focusSize > 0) {
        convoSize = focusSize;
      }
      updateTime = timeNow;
      if (!this.startTime) {
        this.startTime = timeNow; 
      }
      return this;
    };
    
    this.convoHide = function() {
      track();
      const timeNow = new Date().getTime();
      //console.log("<< " + activityId + " convoHidden: " + this.convoShown + " << " + timeNow);
      fixateConvoTime(timeNow);
      updateTime = timeNow;        
      return this;
    };
    
    this.contentHit = function() {
      track();
      updateTime = new Date().getTime();
      if (this.contentHits === null) {
        this.contentHits = 1;
      } else {
        this.contentHits++;
      }
    };
    
    this.convoHit = function() {
      track();
      updateTime = new Date().getTime();
      if (this.convoHits === null) {
        this.convoHits = 1;
      } else {
        this.convoHits++;
      }
    };
    
    this.profileHit = function() {
      track();
      updateTime = new Date().getTime();
      if (this.profileHits === null) {
        this.profileHits = 1;
      } else {
        this.profileHits++;
      }
    };
    
    this.appHit = function() {
      track();
      updateTime = new Date().getTime();
      if (this.appHits === null) {
        this.appHits = 1;
      } else {
        this.appHits++;
      }
    };
    
    this.linkHit = function() {
      track();
      updateTime = new Date().getTime();
      if (this.linkHits === null) {
        this.linkHits = 1;
      } else {
        this.linkHits++;
      }
    };
    
    this.fixate = function() {
      // Stop tracker timer
      if (tracker) {
        clearTimeout(tracker);
      }
      if (this.startTime > 0) {
        this.stopTime = new Date().getTime();
        // Calc rest of content/convo time
        fixateContentTime(this.stopTime);
        this.contentShown = Math.round(contentShownMs / 1000); 
        fixateConvoTime(this.stopTime);
        this.convoShown = Math.round(convoShownMs / 1000);
        // Calc full time
        const totalShownMs = this.stopTime - this.startTime;
        if (totalShownMs >= QUICK_ATTENTION_TIME 
            || this.linkHits > 0 || this.appHits > 0 || this.profileHits > 0 || this.contentHits > 0 || this.convoHits > 0 ) {
          // Convert totalShown from millis to seconds
          this.totalShown = Math.round(totalShownMs/1000);
          // Save the focus to server (or use local pool to send to server in batches)
          saveFocus(this);
          log("<< " + activityId + " fixate: " + JSON.stringify(this));
        }        
      }
      // Finally cleanup
      focused.delete(activityId);
      return this;
    };
  }
  
  function getActivity(activityId) {
    return focused.get(activityId);
  }
  
  function createActivity(activityId) {
    const initTime = new Date().getTime();
    var focus = new Focus(activityId, initTime);
    focused.set(activityId, focus);
    debug(">> " + activityId + " init: " + initTime);
    return focus;
  }
  
  function trackActivity(activityId) {
    let focus = getActivity(activityId);
    if (!focus) {
      focus = createActivity(activityId);
    }
    return focus;
  }
  
  function viewTop() {
    let top = $window.scrollTop();
    // exclude toolbars
    let toolbarHeight = $("#UIToolbarContainer").outerHeight();
    if (toolbarHeight) {
      top += toolbarHeight;
    }
    let spaceMenuHeight = $("#UISpaceMenu").outerHeight();
    if (spaceMenuHeight) {
      top += spaceMenuHeight;
    }
    return top;
  }

  function initHitHandlers($elem) {
    const $contentBox = $elem.find(".contentBox");
    if (!$contentBox.data("smartactivityTracked")) {
      $contentBox.data("smartactivityTracked", true);
      // Working parts of an activity:
      const $activityAvatar = $elem.find(".activityAvatar");
      const $heading = $elem.find(".heading");
      const $contents = $elem.find(".description, .introBox");
      const $previews = $elem.find(".previews");
      const $actionBar = $elem.find(".actionBar");
      const $likes = $elem.find(".listLikedBox");
      const $comments = $elem.find(".commentBox");
    
      const activityId = getActivityId($elem);
      function contentHitHandler() {
        trackActivity(activityId).contentHit();
      }
      function convoHitHandler() {
        trackActivity(activityId).convoHit();
      }
      function appHitHandler() {
        trackActivity(activityId).appHit();
      }
      function profileHitHandler() {
        trackActivity(activityId).profileHit();
      }
      function linkHitHandler() {
        const $link = $(this);
        const href = $link.attr("href");
        if (href) {
          if (href.startsWith("/") || href.startsWith(prefixUrl)) {
            if (href.indexOf("/profile/") > 0) {
              profileHitHandler();
            } else {
              appHitHandler();
            }
          } else if (href.startsWith("javascript:") ) {
            appHitHandler();
          } else {
            trackActivity(activityId).linkHit();
          }              
        } else {
          // Case of elem with onclick, hard figure something better
          appHitHandler();
        }
      }
      
      function profileHoverInHandler() {
        $(this).data("smartactivityHoverTime", new Date().getTime());
      }
      function profileHoverOutHandler(chainHandler) {
        if (new Date().getTime() - $(this).data("smartactivityHoverTime") >= PROFILE_ATTENTION_TIME) {
          profileHitHandler();
          if (chainHandler) {
            chainHandler.call(this);
          }
        }
        $(this).removeData("smartactivityHoverTime");
      }
      function profileHoverOutContentHandler() {
        profileHoverOutHandler.call(this, contentHitHandler);
      }
      function profileHoverOutConvoHandler() {
        profileHoverOutHandler.call(this, convoHitHandler);
      }
      function reinitTrackers() {
        initHitHandlers($elem);
      }
      
      const hitEvents = "click contextmenu";
      
      // Clicks in avatar and heading's author produce profile hits:
      $().add($activityAvatar.find("a")).add($heading.find(".ownerName a")).on(hitEvents, profileHitHandler)
            .hover(profileHoverInHandler, profileHoverOutContentHandler);
      // Click on space produces app hit:
      $heading.find(".spaceName, .space-avatar").on(hitEvents, appHitHandler);
      // TODO should we treat opening of an activity (or its comment) on a dedicated page as user attention?
      // That dedicated page will track time and all hits on it. 
      //$heading.find(".dataInfor a").on(hitEvents, appHitHandler);
      // Clicks inside the content (description and introBox) produce link hits when it goes outside 
      // and app hits when inside the portal
      $contents.find("a").on(hitEvents, linkHitHandler).on(hitEvents, contentHitHandler); // All links inside content
      $contents.find("a[href*='/profile/']").hover(profileHoverInHandler, profileHoverOutContentHandler); // User profiles in the content
      // Clicks in preview all app hits:
      $previews.find("a").on(hitEvents, appHitHandler).on(hitEvents, contentHitHandler); // All links inside preview are app hits
      $previews.find(".fileVersion").on("click", appHitHandler).on("click", contentHitHandler); // Doc version
      // Conversation (likes/comments) hits:
      // Clicks in actionBar are app hits and ones at the left are content hits, right ones are convo hits.
      // TODO distinguish links to app pages (forum, docs, calendar etc) and direct content access (download, edit etc) 
      $actionBar.find(".pull-left.statusAction a").on(hitEvents, appHitHandler).on(hitEvents, contentHitHandler); // <<<<< TODO
      const $rightActions = $actionBar.find(".pull-right.statusAction");
      $rightActions.find("a[id^='CommentLink'], a[id^='LikeLink']").on("click", convoHitHandler);
      $rightActions.find(".SendKudosButtonTemplate a, .SendKudosButtonTemplate button").on("click", convoHitHandler); // kudos on activity
      $likes.find(".listLiked a").on(hitEvents, profileHitHandler).on(hitEvents, convoHitHandler).hover(profileHoverInHandler, profileHoverOutConvoHandler); // likers avatars
      // it's 'View all N comments' link, when clicked all activity element will be relaoded and need reinit hit trackers
      $comments.find(".commentListInfo a").on(hitEvents, convoHitHandler).on("click", reinitTrackers);
      const $commentList = $comments.find(".commentList");
      // it's 'Show all replies' link, when clicked all activity element will be relaoded and need reinit hit trackers
      $commentList.find(".subCommentShowAllLink a").on(hitEvents, convoHitHandler).on("click", reinitTrackers); 
      $commentList.find(".commmentLeft a").on(hitEvents, profileHitHandler).hover(profileHoverInHandler, profileHoverOutConvoHandler); // author avatar
      $commentList.find(".commentRight .author a").on(hitEvents, profileHitHandler).hover(profileHoverInHandler, profileHoverOutConvoHandler); // author title
      $commentList.find(".commentRight .contentComment a").on(hitEvents, linkHitHandler).on(hitEvents, convoHitHandler); // Comment content links
      $commentList.find(".commentRight .contentComment a[href*='/profile/']").hover(profileHoverInHandler, profileHoverOutConvoHandler); // user profiles in comments
      const $actionCommentBar = $commentList.find(".commentRight .actionCommentBar");
      $actionCommentBar.find(".likeCommentLink, .likeCommentCount, .subComment").on("click", convoHitHandler); // comment Like/Comment
      $actionCommentBar.find(".SendKudosButtonTemplate a, .SendKudosButtonTemplate button").on("click", convoHitHandler); // kudos on comments
      
      // TODO collect hits in activity preview open
    }
  }
  
  function trackHits($activities) {
    $activities.each(function() {
      const $elem = $(this);
      initHitHandlers($elem);
    });
  }
  
  let lastScrollPosition = null;
  function trackFocus($activities) {
    // document.documentElement.clientHeight - $(window).innerHeight()
    // document.body.clientHeight - $(document).innerHeight()
    const topOfView = viewTop();
    let prevScrollPosition = lastScrollPosition;
    lastScrollPosition = topOfView;
    
    // XXX In case of focus change we need act even if not scrolled 
    //if (lastScrollPosition !== prevScrollPosition) {
    const bottomOfView = $window.scrollTop() + $window.innerHeight();
    const heightOfView = bottomOfView - topOfView;
    
    // Focusport of viewport
    const viewLength = 15;
    let sectorsToTop, sectorsToBottom;
    const viewSectorHeight = heightOfView / viewLength;
    //console.log(">> heightOfView: " + heightOfView + ", viewSector: " + viewSectorHeight);
    // Focusport lies between, inclusive of available viewport:
    if (heightOfView > 900) {
      // 900px > viewport: 1/15 and 13/15
      sectorsToTop = 1;
      sectorsToBottom = 13;
    } else {
      // 900px < viewport: 1/15 and 14/15 
      sectorsToTop = 1;
      sectorsToBottom = 14;
    }
    
    const focusportTop = topOfView + viewSectorHeight * sectorsToTop;
    const focusportBottom = topOfView + viewSectorHeight * sectorsToBottom;
    const focusportSize = sectorsToBottom - sectorsToTop;
    const minimalAttentionHeight = viewSectorHeight * 1;
    
    function shownHeight(top, bottom) {
      if (bottom < focusportTop || top > focusportBottom) {
        return 0;
      } else {
        let shown = bottom - top; 
        if (bottom > focusportBottom) {
          shown -= bottom - focusportBottom;
        }
        if (top < focusportTop) {
          shown -= focusportTop - top;                
        }
        return shown;          
      }
    }
    
    function shownSize(top, bottom) {
      const size = Math.round((bottom - top) / viewSectorHeight);
      let rate = Math.round(size * 100 / focusportSize) / 100;
      return rate > 0.5 ? 1 + rate : 1;
    }
    
    // ***** Debug
    if (eXo.smartactivity.debug) {
      let $focusport = $("#focusport-dev");
      if ($focusport.length == 0) {
        $("body").append("<div id='focusport-dev' style='display: none;'></div>");
        $focusport = $("#focusport-dev");
      }
      $focusport.css({
        top : focusportTop - $window.scrollTop(),
        height : focusportBottom - focusportTop
      });
      $focusport.show();        
    }
    
    $activities.each(function() {
      const $elem = $(this);
      const activityId = getActivityId($elem);
      
      // ***** Debug
      if (eXo.smartactivity.debug) {
        let $dataInfor = $elem.find(".dataInfor");
        if ($dataInfor.find(".activityid-dev").length == 0) {
          $dataInfor.append("<span class='activityid-dev pull-right'>" + activityId + "</span>");
        }
      }
      
      //
      const offset = $elem.offset();
      const elementTop = offset.top;
      const elementBottom = offset.top + $elem.innerHeight();
      
      // Sequence of events for each activity block: E1 -> C -> A or B -> D -> E2
      if (focusportBottom <= elementTop) {
        // E1) the element is not visible: it's hidden at bottom
        const focus = getActivity(activityId);
        if (focus) {
          focus.fixate();
        }
      } else if (focusportTop >= elementBottom) {  
        // E2) the element is not visible: it's hidden at top
        const focus = getActivity(activityId);
        if (focus) {
          focus.fixate();            
        }
      } else {
        // A) The element is fully visible in focusport (small activity)
        // B) the element is partially visible, top and bottom is hidden (element block bigger of the focusport)
        // C) the element is partially visible, bottom is hidden
        // D) the element is partially visible, top is hidden
        
        // Find location for content and comments blocks
        const $heading = $elem.find(".heading");
        const $actionBar = $elem.find(".actionBar");
        const contentTop = elementTop + ($heading.length > 0 ? $heading.outerHeight() : 0);
        const actionBarOffset = $actionBar.offset();
        const contentBottom = actionBarOffset ? actionBarOffset.top : elementBottom;
        const convoTop = (function() {
          const $likes = $elem.find(".listLikedBox");
          if ($likes.length > 0) {
            return $likes.offset().top;
          } else {
            const $comments = $elem.find(".commentBox");
            if ($comments.length > 0 && $comments.outerHeight() > 0) {
              return $comments.offset().top;
            }            
          }
          return elementBottom;
        })();
        const convoBottom = elementBottom;
        
        // Size of visible parts to whole focusport
        const contentSize = shownSize(contentTop, contentBottom);
        const convoSize = shownSize(convoTop, convoBottom);
        
        // Track user focus on activity (and its parts)
        const focus = trackActivity(activityId);
        //const isActivityPage = $activities.length == 1 && window.location.pathname.startsWith("/portal/intranet/activity");
        //if (isActivityPage) {
          // TODO make app hit? will it be correct? 
          // Should we care about showing an activity on dedicated page as such? Its *Shown time will be tracked anyway.
        //}
        const contentShownHeight = shownHeight(contentTop, contentBottom);
        if (contentShownHeight >= minimalAttentionHeight) {
          focus.contentShow(contentSize);
        } else {
          focus.contentHide();
        }
        const convoShownHeight = shownHeight(convoTop, convoBottom);
        // If content wasn't shown, then it's comments at the top of focusport from higher (not shown) activity
        if (convoShownHeight >= (contentShownHeight > 0 ? minimalAttentionHeight : minimalAttentionHeight * 2)) {
          focus.convoShow(convoSize);
        } else {
          focus.convoHide();
        }
          
        // TODO cleanup
//        if (focusportBottom >= elementBottom && focusportTop <= elementTop) {
//          // A) The element is fully visible in focusport (small activity)
//          trackActivity(activityId).contentShown().convoShown();
//        } else {
//            if (focusportBottom < elementBottom && focusportTop > elementTop) {
//              // B) the element is partially visible, top and bottom is hidden (element block bigger of the focusport)
//              trackFocus();
//            } else if (focusportBottom < elementBottom && focusportTop < elementTop) {
//              // C) the element is partially visible, bottom is hidden
//              //trackActivity(activityId).contentShown();
//              trackFocus();
//            } else if (focusportBottom > elementBottom && focusportTop > elementTop) {
//              // D) the element is partially visible, top is hidden
//              //trackActivity(activityId).convoShown();
//              trackFocus();
//            } else {
//              // E*) the element not visible, but it should not happen here (see upper if-block)
//              console.log("WARN >>>> Unexpected not visible activity event for " + activityId);
//            } 
//          }
      }
      
      // TODO collect focus/hits in activity preview open
    });      
    //} // otherwise, is it a resize?
  }
  
  let enableTrackers = false;
  function startTrackersFor($activities) {
    if (!$activities || $activities.length == 0) {
      $activities = $(".activityStream");  
    }
    trackFocus($activities);
    trackHits($activities);
  }
  function startTrackers() {
    startTrackersFor();
  }
  function stopTrackers() {
    focused.forEach(function(focus) {
      focus.fixate();
    });
    focused.clear();
  }

  function getRelevantIcon() {
    return '<li><a rel="tooltip" href="javascript:void(0);" class="relevance-tooltip" data-placement="bottom" data-original-title="'
        + relevantText + '"><span class="relevance relevance-relevant uiIconBlue"></span>&nbsp;&nbsp;&nbsp;</a></li>';
  }

  function getIrrelevantIcon() {
    return '<li><a rel="tooltip" href="javascript:void(0);" class="relevance-tooltip"  data-placement="bottom" data-original-title="'
        + neutralText + '"><span class="relevance relevance-irrelevant uiIconBlue"></span>&nbsp;&nbsp;&nbsp;&nbsp;</a></li>';
  }

  function getDefaultIcon() {
    return '<li><a rel="tooltip" href="javascript:void(0);" class="relevance-tooltip" data-placement="bottom" data-original-title="'
        + irrelevantText + '"><span class="relevance relevance-default"></span>&nbsp;&nbsp;&nbsp;</a></li>';
  }

  $document.ready(function() {
    // Social stream portlet for activities relevance & tracking 
    let $streamPortlet;
    // Searching for observation target
    if ($(".uiUserActivityStreamPortlet").length > 0) {
      $streamPortlet = $(".uiUserActivityStreamPortlet").closest(".PORTLET-FRAGMENT");
    } else if ($(".uiSpaceActivityStreamPortlet").length > 0) {
      $streamPortlet = $(".uiSpaceActivityStreamPortlet").closest(".PORTLET-FRAGMENT");
    }

    if ($streamPortlet && $streamPortlet.length > 0) {
      // 1) Relevance button and its icons
      // Set initial state of the icons
      updateStateOfIcons($(".activityStream")); // was boxContainer
      // Observe changes in the stream to add icons to new activities
      const observer = new MutationObserver(function(mutations) {
        mutations.forEach(function(mutation) {
          if ($(mutation.addedNodes).find(".actionBar").length > 0) {
            const $activities = $(mutation.target).find(".actionBar").closest(".activityStream");
            updateStateOfIcons($activities);
            // Update activities hits trackers: we don't need this here, trackers will be activated on scroll event
            //if (enableTrackers) {
              // In case of Ajax loaded stream/activity parts (e.g. all comments), we need ensure the activity stats will be collected 
              //startTrackersFor($activities);
            //}
          }
        });
        // TODO preview trackers (focus and hits)
      });
      // Start observing
      observer.observe($streamPortlet.get(0), {
        childList : true,
        subtree : true
      });
    }
  });
  
  return {
    init : function(context) {
      if (context) {
        const userId = context.userId;
        if (userId == currentUserId) {
          log("Already initialized user: " + userId);
        } else if (userId) {
          currentUserId = userId;
          log("Initialize user: " + userId);
          // TODO Do we need i18n here?
          //if (userMessages) {
          //  messages = userMessages;
          //}
          cCometD.configure({
            "url" : prefixUrl + context.cometdPath,
            "exoId" : userId,
            "exoToken" : context.cometdToken,
            "maxNetworkDelay" : 30000,
            "connectTimeout" : 60000
          });
          cometdContext = {
            "exoContainerName" : context.containerName
          };
          cometd = cCometD;
          
          // Enable focus/hits trackers
          enableTrackers = true;
          $document.ready(function() {
            // Start trackers with a delay to let the page fully load and focus/scroll
            setTimeout(function() {
              startTrackers();
              $window.on("resize scroll", startTrackers); // was also 'DOMContentLoaded load', why and for which browser: IE/Edge/Safari?
              $window.on("focus", startTrackers);
              $window.on("blur", stopTrackers);
              $window.on("beforeunload", stopTrackers);
              $window.on("unload", stopTrackers);
            }, PAGE_LOAD_ATTENTION_TIME);
          });
        } else {
          log("Cannot initialize user: " + userId);
        }
      } else {
        log("Cannot initialize user: null context");
      }
    }
  };
})($, cCometD);
