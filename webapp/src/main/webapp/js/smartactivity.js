(function($) {
  "use strict";

  var relevantText = "Click to mark as irrelevant";
  var irrelevantText = "Click to mark as relevant";
  var neutralText = "Click to mark as neutral";
  
  // Adds onClick listener to the elements
  function addRelevanceOnClickListener(elements) {
    $(elements).click(function() {
      var $elem = $(this);
      if ($elem.attr("onClick") == undefined) {
        // The link contains activityId
        var activityId = $elem.parents(".activityStream").attr("id");
        activityId = activityId ? activityId.replace("activityContainer", "") : "";

        if ($elem.hasClass("relevance-default")) {
          console.log("Action: relevant | ID: " + activityId);
          sendRelevance(activityId, true);
          $elem.removeClass("relevance-default");
          $elem.toggleClass('relevance-relevant');
          $elem.toggleClass('uiIconBlue');

          $elem.closest('a.relevance-tooltip').attr("data-original-title", relevantText);
        } else if ($elem.hasClass("relevance-relevant")) {

          console.log("Action: irrelevant | ID: " + activityId);
          sendRelevance(activityId, false);
          $elem.removeClass("relevance-relevant");
          $elem.toggleClass('relevance-irrelevant');

          $elem.closest('a.relevance-tooltip').attr("data-original-title", neutralText);
        } else {
          console.log("Action: default relevance | ID: " + activityId);
          sendRelevance(activityId, null);
          $elem.removeClass("relevance-irrelevant");
          $elem.toggleClass('relevance-default');
          $elem.removeClass('uiIconBlue');
          $elem.closest('a.relevance-tooltip').attr("data-original-title", irrelevantText);
        }
      }
    });
  }

  // Finds out page base url
  var pageBaseUrl = function(theLocation) {
    if (!theLocation) {
      theLocation = window.location;
    }

    var theHostName = theLocation.hostname;
    var theQueryString = theLocation.search;

    if (theLocation.port) {
      theHostName += ":" + theLocation.port;
    }

    return theLocation.protocol + "//" + theHostName;
  };

  // Sends information about relevance of the activity to the server
  function sendRelevance(activityId, relevant) {
    // The object of relevance to be sent to the server
    var relevance = {
      "userId" : eXo.env.portal.userName,
      "activityId" : activityId
    };
    if (relevant != null) {
      relevance.relevant = relevant;
    }
    postRelevance(relevance);
  }

  // Updates state of the icons. Accepts any parent div of an icon element
  function updateStateOfIcons(iconsParentDiv) {

    var prefixUrl = pageBaseUrl(location);

    // Iterates through each activity block and inserts the relevance icon
    $(iconsParentDiv).find('.actionBar > .statusAction.pull-right').each(function() {
      var $elem = $(this);

      // The link contains activityId
      var activityId = $elem.parents(".activityStream").attr("id");
      activityId = activityId ? activityId.replace("activityContainer", "") : "";

      var userId = eXo.env.portal.userName

      // If there is already icon move to the next block
      if ($elem.find('.relevance').length !== 0) {
        return;
      }

      var promisedRelevance = getRelevance(userId, activityId);

      // If server responded with relevance
      promisedRelevance.done(function(data) {
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
        addRelevanceOnClickListener($elem.find('.relevance'));
        // Call tooltip handler
        $elem.find('a.relevance-tooltip').tooltip();
      });

      // If server responded with error
      promisedRelevance.fail(function(XMLHttpRequest) {
        // If user hasn't checked relevance for the activity
        if (XMLHttpRequest.status == 404) {
          // Add default icon
          $elem.prepend(getDefaultIcon());
          // Add onClickListener to new icon
          addRelevanceOnClickListener($elem.find('.relevance'));
        } else {
          console.log('Smart Activity: Error status: ' + XMLHttpRequest.status + ', text: ' + XMLHttpRequest.statusText);
        }
        // Call tooltip handler
        $elem.find('a.relevance-tooltip').tooltip();
      });
    });
  }

  var postRelevance = function(relevance) {
    var prefixUrl = pageBaseUrl(location);

    var request = $.ajax({
      url : prefixUrl + "/portal/rest/smartactivity/relevancy/" + relevance.userId + "/" + relevance.activityId,
      type : 'post',
      contentType : 'application/json',
      data : JSON.stringify(relevance)
    });
    return request;
  };

  var getRelevance = function(userId, activityId) {
    var prefixUrl = pageBaseUrl(location);

    var request = $.ajax({
      url : prefixUrl + "/portal/rest/smartactivity/relevancy/" + userId + "/" + activityId,
      type : 'get'
    });
    return request;
  };

  var getRelevantIcon = function() {
    return '<li><a rel="tooltip" href="javascript:void(0);" class="relevance-tooltip" data-placement="bottom" data-original-title="'
        + relevantText + '"><span class="relevance relevance-relevant uiIconBlue"></span>&nbsp;&nbsp;&nbsp;</a></li>';
  }

  var getIrrelevantIcon = function() {
    return '<li><a rel="tooltip" href="javascript:void(0);" class="relevance-tooltip"  data-placement="bottom" data-original-title="'
        + neutralText + '"><span class="relevance relevance-irrelevant uiIconBlue"></span>&nbsp;&nbsp;&nbsp;&nbsp;</a></li>';
  }

  var getDefaultIcon = function() {
    return '<li><a rel="tooltip" href="javascript:void(0);" class="relevance-tooltip" data-placement="bottom" data-original-title="'
        + irrelevantText + '"><span class="relevance relevance-default"></span>&nbsp;&nbsp;&nbsp;</a></li>';
  }

  $(document).ready(function() {
    var $observerTarget;

    // Searching for observation target
    if ($(".uiUserActivityStreamPortlet").length > 0) {
      $observerTarget = $(".uiUserActivityStreamPortlet").closest(".PORTLET-FRAGMENT");
    } else if ($(".uiSpaceActivityStreamPortlet").length > 0) {
      $observerTarget = $(".uiSpaceActivityStreamPortlet").closest(".PORTLET-FRAGMENT");
    }

    if ($observerTarget && $observerTarget.length > 0) {
      // Set initial state of the icons
      updateStateOfIcons($(".boxContainer"));

      // Observe changes in the stream to add icons to new activities
      var observer = new MutationObserver(function(mutations) {
        mutations.forEach(function(mutation) {
          if ($(mutation.addedNodes).find(".actionBar").length > 0) {
            updateStateOfIcons($(mutation.target).find(".actionBar").closest('.boxContainer'));
          }
        });
      });
      // Start observing
      observer.observe($observerTarget.get(0), {
        childList : true,
        subtree : true
      });
    }
  });
  
})($);
