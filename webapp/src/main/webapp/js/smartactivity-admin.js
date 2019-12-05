(function ($, vuetify, vue, eXoVueI18n, googleCharts) {

  //Defines the base page URL
  var pageBaseUrl = function (theLocation) {
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

  //Loading the google charts, 'google' is the global variable (it depends the google charts)
  google.charts.load("current", {"packages" : ["corechart"]});

  //The subtable chart
  var subtableChart;

  //The selected subtable row flag (boolean)
  var selectedSubtableRow = false;

  //The base page URL
  var prefixUrl = pageBaseUrl(location);

  //The stream selected default value
  var streamSelected = "All streams";

  //The substream selected default value
  var substreamSelected = null;

  //The substream selected default id value
  var substreamSelectedId = null;

  //The stream point selected data
  var streamPointSelectedData = [];

  //Values for the stream selector
  var streamSelectorVars = [
    "All streams",
    "Space",
    "User",
  ];

  //The time scale default value
  var timeScaleValue = 3;
  //The time scale default value in minutes
  var timeScaleMinutes = 60;

  //Values for the time scaling
  var timeScaleSettingsVars = [
    "10 min",
    "20 min",
    "30 min",
    "1 hour",
    "4 hours",
    "1 day",
    "1 week",
  ];

  //The main statistic table
  var mainTable;

  //The last opened main table row
  var lastOpenedMainTableRow;

  //Values for the subtable scaling
  var activitiesStreamPrettyIdMaxString = "";
  var activitiesTitleMaxString = "";

  //Activity stats max total shown value (for the general scaling of the main table charts)
  var maxTotalShown;


  //Define the debugging properties
  if (eXo) {
    if (!eXo.smartactivity) {
      eXo.smartactivity = {
        debug : false
      };
    }
  }
  eXo.smartactivity.debug = false; // for dev purpose


  //The start block. Defines all elements when the document ready
  $(document).ready(function () {

    addActiveClassToMenuNavigation();

    debug("prefixUrl: " + prefixUrl);

    defineMainTable();

    defineTimeScaler();

    defineStreamSelector();

    //Get all default table data
    substreamSelected = null;
    substreamSelectedId = null;
    mainTable.getDataForTheTable();

    debug("smartactivity-admin.js and page ready");
  });

  function defineMainTable() {
    mainTable = new Vue({
      el : "#app-smartactivity-table-vue-and-vuetify",
      vuetify : new Vuetify(),
      data() {
        return {
          expanded : [],
          search : "",
          subtableSearch : "",
          singleExpand : true,
          headers : [
            {text : "", value : "data-table-expand"},
            {text : "Total Focus Chart", sortable : false, value : "focus_chart_data"},
            {
              text : "Activity Data (Title)",
              align : "left",
              sortable : false,
              value : "activity_title",
            },
            {text : "Updated", value : "activity_updated"},
            {text : "Total Focus", value : "totalShown"},
            {text : "Content Focus", value : "contentShown"},
            {text : "Convo Focus", value : "convoShown"},
            {text : "Content Hits", value : "contentHits"},
            {text : "Convo Hits", value : "convoHits"},
            {text : "App Hits", value : "appHits"},
            {text : "Profile Hits", value : "profileHits"},
            {text : "Link Hits", value : "linkHits"},
          ],
          tableVal : [],
          subtableVal : [],
        }
      },
      methods : {
        selectTableRow : function (event) {
          //Deletes the old subtable char if exists
          $("#subtable-chart").remove();

          if (event.value == true) {
            var activityTitle = event.item.activity_title;

            lastOpenedMainTableRow = event.item;

            getActivityFocuses(lastOpenedMainTableRow.activityId);
            debug("Opened row: " + activityTitle);
          }
        },
        getDataForTheTable : function (event) {
          getUserFocuses(streamSelected, substreamSelectedId);
        },
        updateTableVal : function (newData) {
          //Deletes values of the table
          this.tableVal.splice(0, this.tableVal.length);

          for (var index in newData) {
            findMaxActivityTitleAndStreamPrettyIdStringsForSubtableScaling(newData[index]);
            this.tableVal.push(newData[index]);
          }

          function findMaxActivityTitleAndStreamPrettyIdStringsForSubtableScaling(elem) {
            if (activitiesStreamPrettyIdMaxString.length < elem.activityStreamPrettyId.length) {
              activitiesStreamPrettyIdMaxString = elem.activityStreamPrettyId;
            }

            if (activitiesTitleMaxString.length < elem.activity_title.length) {
              activitiesTitleMaxString = elem.activity_title;
            }
          }
        },
        //Deletes values of the subtable
        clearSubtableVal : function () {
          this.subtableVal.splice(0, this.subtableVal.length);
        },
        updateSubtableVal : function (newData) {
          for (var index in newData) {
            copyActivityDataToActivityFocus(newData[index]);
            this.subtableVal.push(newData[index]);
          }

          function copyActivityDataToActivityFocus(activityFocus) {
            activityFocus.activity_title = activitiesTitleMaxString;
            activityFocus.activityStreamPrettyId = activitiesStreamPrettyIdMaxString;
            activityFocus.activityCreated = lastOpenedMainTableRow.activityCreated;
            activityFocus.activityUpdated = lastOpenedMainTableRow.activityUpdated;
          }

          //Async call
          setTimeout(defineSubtableColumnChartSelector, 200);
          setTimeout(defineSubtableRowHoveredPointOnTheChartSelector, 200);
        },
        drawChart : function (item) {
          var id = `chart-div-${item.activityId}-${item.startTime}`;

          var iteration = 0;
          //Async call
          setTimeout(createTableChart, 25, id, item, iteration);
          return id;
        },
        customSort : function (items, index, isDesc) {
          closeExpendedRow();
          //Default custom sort
          if (index.length == 0) {
            var aRating;
            var bRating;

            items.sort((a, b) => {
              aRating = a.totalShown * 60000 / (a.stopTime - a.activity_created);
              bRating = b.totalShown * 60000 / (b.stopTime - b.activity_created);

              return bRating - aRating;
            });
          } else {
            items.sort((a, b) => {
              if (typeof a[index] == "string") {
                if (!isDesc[0]) {
                  return a[index].localeCompare(b[index]);
                } else {
                  return b[index].localeCompare(a[index]);
                }
              } else {
                if (!isDesc[0]) {
                  return a[index] < b[index] ? -1 : 1;
                } else {
                  return b[index] < a[index] ? -1 : 1;
                }
              }
            });
          }

          return items;
        }
      }
    });


    function defineSubtableColumnChartSelector() {
      $(`#subtable td`).on("click", function () {
        var elementClickedColumn = this.cellIndex;

        if (elementClickedColumn > 3) {
          //Clears the selection before
          $("#subtable").find("td").css("background-color", "");

          //Sets background color of the selected column
          $("#subtable tr").each(function () {
            $(this).find("td").eq(elementClickedColumn).css("background-color", "#e6e6e6");
          });

          var dataHeader;

          //Selects needed header for the subtable chart
          switch (elementClickedColumn) {
            case 4:
              dataHeader = "totalShown";
              break;
            case 5:
              dataHeader = "contentShown";
              break;
            case 6:
              dataHeader = "convoShown";
              break;
            case 7:
              dataHeader = "contentHits";
              break;
            case 8:
              dataHeader = "convoHits";
              break;
            case 9:
              dataHeader = "appHits";
              break;
            case 10:
              dataHeader = "profileHits";
              break;
            case 11:
              dataHeader = "linkHits";
              break;
          }

          //Displays the chart for selected column
          createSubtableChart(mainTable.subtableVal, dataHeader);
        }
      });

      //Adds a shadow over the hovered column
      $("#subtable td").hover(function () {
        var elementHoveredColumn = this.cellIndex;

        if (elementHoveredColumn > 3) {
          $(`#subtable tr`).each(function () {
            $(this).find("td").eq(elementHoveredColumn).css(
              {
                "box-shadow" : "inset 15px 0px 20px -25px rgba(0,0,0,0.45), inset -15px 0px 20px -25px rgba(0,0,0,0.45)"
              }
            );
          });
        }
      }, function () {
        //Clears a shadow over hovered before columns
        $("#subtable").find("td").css({
          "box-shadow" : ""
        });
      });

      //Defines the default column for the chart
      $("#subtable td").eq(4).click();
    }

    function defineSubtableRowHoveredPointOnTheChartSelector() {

      //Handles a click on the subtable row
      $("#subtable tr").click(function () {

        var selectedPoint = subtableChart.getSelection();

        var elementPositionOnTheChart = mainTable.subtableVal.length - $(this).index() - 1;

        $("#subtable tr").css("background-color", "");
        if (!selectedSubtableRow || selectedPoint[0] == undefined || selectedPoint[0].row != elementPositionOnTheChart) {
          $(this).css("background-color", "rgb(238,238,238)");
          subtableChart.setSelection([{row : elementPositionOnTheChart, column : 1}]);
          selectedSubtableRow = true;
        } else {
          selectedSubtableRow = false;
        }

      });

      //Handles a mouse hover over the subtable row
      $("#subtable tr").hover(function (event) {
        if (!selectedSubtableRow) {
          var elementPositionOnTheChart = mainTable.subtableVal.length - $(this).index() - 1;
          subtableChart.setSelection([{row : elementPositionOnTheChart, column : 1}]);
        }
      });
    }
  }

  function defineTimeScaler() {
    new Vue({
      el : "#time-scale",
      vuetify : new Vuetify(),
      data() {
        return {
          value : timeScaleValue,
          timeScaleModel : 3,
          ticksLabels : timeScaleSettingsVars,
        }
      },
      methods : {
        selectTimeScale : function (event) {
          debug("Time scale: " + event);

          closeExpendedRow();

          switch (event) {
            case 0:
              timeScaleMinutes = 10;
              break;
            case 1:
              timeScaleMinutes = 20;
              break;
            case 2:
              timeScaleMinutes = 30;
              break;
            case 3:
              timeScaleMinutes = 60;
              break;
            case 4:
              timeScaleMinutes = 240;
              break;
            case 5:
              timeScaleMinutes = 1440;
              break;
            case 6:
              timeScaleMinutes = 10080;
              break;
          }
          debug("Minutes: " + timeScaleMinutes);
        }
      }
    });
  }

  function defineStreamSelector() {
    new Vue({
      el : "#stream-selector",
      vuetify : new Vuetify(),
      data : () => ({
        stream : streamSelected,
        streams : streamSelectorVars,
      }),
      methods : {
        selectStream : function (event) {
          var beforeStreamSelected = streamSelected;

          streamSelected = event;
          debug("Selected stream: " + event);

          deleteSubstreamSelector();
          streamPointSelectedData.length = 0;

          // Handles the selected stream
          switch (streamSelected) {
            case "All streams":
              substreamSelected = null;
              substreamSelectedId = null;
              break;
            case "Space":
              var subselectorHeader = "All spaces";

              defineSelectedSubstream(beforeStreamSelected, streamSelected, subselectorHeader);

              getPointsOfTheSelectedStream(subselectorHeader, "user-space");
              defaultClickOnSubstreamSelector();
              break;
            case "User":
              var subselectorHeader = "All users";

              defineSelectedSubstream(beforeStreamSelected, streamSelected, subselectorHeader);

              getPointsOfTheSelectedStream(subselectorHeader, "user-connection");
              defaultClickOnSubstreamSelector();
              break;
          }

          //Clears values for subtable scaling
          activitiesStreamPrettyIdMaxString = "";
          activitiesTitleMaxString = "";

          closeExpendedRow();

          mainTable.getDataForTheTable();
        }
      }


    });

    defineStreamSelectorPadding();

    function defineStreamSelectorPadding() {
      $("#stream-selector .VuetifyApp .v-text-field").css("padding-top", "2px");
      $("#stream-selector .VuetifyApp .v-application .v-text-field ").css("padding-top", "2px");
    }

    //Opens the substream selector
    function defaultClickOnSubstreamSelector() {
      $("#substream-selector .v-input__slot").click();
    }

    function defineSelectedSubstream(beforeStreamSelected, streamSelected, subselectorHeader) {
      if (beforeStreamSelected != streamSelected) {
        substreamSelected = subselectorHeader;
        substreamSelectedId = substreamSelected;
      }
    }
  }

  function getPointsOfTheSelectedStream(subselectorHeader, dataClass) {
    streamPointSelectedData.length = 0;

    //Extracts points from the hidden html block
    $(`.${dataClass}`).each(function (index) {
      streamPointSelectedData.push({
        dataValue : $(this).text(),
        streamPointId : $(this).attr("id-value")
      });
    });

    addSubstreamSelector();
    defineSubstreamSelector(subselectorHeader);
  }

  function addSubstreamSelector() {
    $("#stream-selector").after(`<div id="substream-selector" class="VuetifyApp">
                <v-app id="substream-selector-app">
                    <div>
                        <v-select class="custom-select" v-on:input="selectSubstream" 
                          v-on:blur="deleteSubstreamSelector" v-model="substream" :items="substreams"></v-select>
                    </div>
                </v-app>
            </div>`);
  }

  function deleteSubstreamSelector() {
    $("#substream-selector").remove();
  }

  function defineSubstreamSelector(subselectorHeader) {
    var substreamData = [];

    substreamData.push(subselectorHeader);
    for (var element in streamPointSelectedData) {
      substreamData.push(streamPointSelectedData[element].dataValue);
    }

    new Vue({
      el : "#substream-selector",
      vuetify : new Vuetify(),
      data : () => ({
        substream : substreamSelected,
        substreams : substreamData,
      }),
      methods : {
        selectSubstream : function (event) {
          substreamSelected = event;
          substreamSelectedId = event;

          for (let el of streamPointSelectedData) {
            if (el.dataValue == substreamSelected) {
              substreamSelectedId = el.streamPointId;
              break;
            }
          }

          //Clear values for subtable scaling
          activitiesStreamPrettyIdMaxString = "";
          activitiesTitleMaxString = "";

          closeExpendedRow();
          this.deleteSubstreamSelector();

          debug("Selected substream: " + event);
          debug("Selected substreamId: " + substreamSelectedId);

          mainTable.getDataForTheTable();
        },
        deleteSubstreamSelector : function () {
          deleteSubstreamSelector();
        }
      }
    });

    //Async call
    setTimeout(defineClickOutsideSubstreamSelectorHandler, 200);

    function defineClickOutsideSubstreamSelectorHandler() {
      $("#time-scale-app .menuable__content__active").first().mouseleave(function (event) {
        $("#time-scale-app .menuable__content__active").first().find("div[aria-selected='true']").click();
      });
    }
  }

  function getUserFocuses(stream, substream) {

    $.ajax({
      async : true,
      type : "GET",
      url : prefixUrl + `/portal/rest/smartactivity/stats/userfocus/${stream}/${substream}`,
      contentType : "application/json",
      dataType : "json",
      success : successF,
      error : errorF
    });

    function successF(data, textStatus, jqXHR) {

      var tableData = data.activityStatsEntities;
      maxTotalShown = data.maxTotalShown;

      //Updates loaded data titles in order to fits needed style
      tableData.forEach(function (obj) {
        obj.activity_created = obj.activityCreated;
        obj.activity_updated = obj.activityUpdated;
        obj.activity_updated = obj.activityUpdated;
        obj.activity_updated = obj.activityUpdated;
        changeObjectPropertyName(obj, "focusChartData", "focus_chart_data");
        changeObjectPropertyName(obj, "activityTitle", "activity_title");
      });

      mainTable.updateTableVal(tableData);

      debug("getUserFocuses ajax request success result");
    }

    function errorF(jqXHR, textStatus, errorThrown) {
      log("getUserFocuses error textStatus:  " + textStatus);
      log("getUserFocuses errorThrown:  ", errorThrown);
    }
  }

  function createSubtableChart(subtableData, dataHeader) {

    if (subtableData.length != 0) {
      var subtableChartHeight = 38;

      //Dynamic subtable chart height scaling
      for (var row = 2; row <= 7; ++row) {
        if (subtableData.length >= row) {
          subtableChartHeight += 35;
        } else {
          break;
        }
      }

      //Removes the subtable chart if exists
      $("#subtable-chart").remove();

      //Adds the subtable chart block
      $("#subtable-column")
        .prepend(`<div id="subtable-chart" style="height: ${subtableChartHeight}px;
                                                margin-bottom: -${subtableChartHeight + 3}px" class="subchart"></div>`);

      var headersAndData = [["Time", "Value"]];

      var startTime;
      var valueInPoint;

      for (var i = subtableData.length - 1; i >= 0; --i) {
        startTime = subtableData[i].localStartTime;
        valueInPoint = Number(subtableData[i][dataHeader]);
        headersAndData.push([startTime, valueInPoint]);
      }

      var data = google.visualization.arrayToDataTable(
        headersAndData
      );

      //The chart options
      var options = {
        legend : "none",
        hAxis : {
          titleTextStyle : {color : "#333"},
          allowContainerBoundaryTextCufoff : false,
          textStyle : {fontSize : 12}
        },
        vAxis : {minValue : 0},
        tooltip : {trigger : 'selection'}
      };

      subtableChart = new google.visualization.AreaChart(document.getElementById("subtable-chart"));
      subtableChart.draw(data, options);


      //Deletes superfluous time titles under the subtable chart
      if ($("#subtable-chart text[font-size='12']").length == (headersAndData.length - 1) * 2) {
        $("#subtable-chart text[font-size='12']:even").remove();
      }

      var neededToReplaceText = $("#subtable-chart text[font-size='12']");

      //The subtable chart time title scaling
      var elementPositionCoeff = Math.floor(headersAndData.length / neededToReplaceText.length);
      var shift = elementPositionCoeff - 1;

      //Changes the default subtable chart time titles
      $("#subtable-chart text[font-size='12']").each(function (index) {
        if (headersAndData != undefined && headersAndData[index + 1] != undefined) {
          var valueOfTheDate = headersAndData[(index + 1) * elementPositionCoeff - shift][0];
          var hoursAndMinutes = valueOfTheDate.substring(valueOfTheDate.length - 8, valueOfTheDate.length - 3);
          $(this).first().text(hoursAndMinutes);
        }
      });

      /*//search selected point

      google.visualization.events.addListener(chart, "onmouseover", function (e) {
        findHoveredStatistic(data, e.row);
      });

      $("#subtable-chart").mouseleave(function () {
          mainTable.subtableSearch = "";
        }
      );*/
    }

    /*function findHoveredStatistic(chartData, element) {
      debug("hovered the chart point");

      //search hovered point
      mainTable.subtableSearch = chartData.jc[element][0].gf;
    }*/
  }

  function changeObjectPropertyName(obj, oldKey, newKey) {
    if (oldKey !== newKey) {
      Object.defineProperty(obj, newKey,
        Object.getOwnPropertyDescriptor(obj, oldKey));
      delete obj[oldKey];
    }
  }

  function createTableChart(id, item, iteration) {

    if (iteration < 100) {
      if (google != undefined && google.visualization != undefined
        && google.visualization.arrayToDataTable != undefined && google.visualization.AreaChart != undefined) {
        var chartBlock = $(`#${id}`);

        if (chartBlock.length) {
          var headersAndData = [["Time", "Focus"]];

          var focusChartDatesAndValues = item.focus_chart_data;

          focusChartDatesAndValues.forEach(function (elem) {
            elem[1] = Number(elem[1]);
            headersAndData.push(elem);
          });

          var data = google.visualization.arrayToDataTable(
            headersAndData
          );

          var options = {
            legend : "none",
            hAxis : {
              allowContainerBoundaryTextCufoff : true,
              textStyle : {color : "#FFF", fontSize : 0},
              showTextEvery : 0,
              tiks : [],
              baseline : {
                color : "green"
              },
              gridlines : {
                color : "#FFF"
              },
              minorGridlines : {
                color : "#FFF"
              }
            },
            vAxis : {
              minValue : 0,
              maxValue : maxTotalShown,
              textStyle : {
                color : "#FFF"
              },
              gridlines : {
                color : "#FFF"
              },
              minorGridlines : {
                color : "#FFF"
              },
              showTextEvery : 0,
              baseline : {
                color : "#FFF"
              },
            },
            lineWidth : 0,
          };

          var chart = new google.visualization.AreaChart(document.getElementById(id));
          chart.draw(data, options);

          chartBlock.parent().parent().css("height", "max-content");
        }
      } else {
        //Async call
        setTimeout(createTableChart, 50, id, item, iteration);
      }
    }
  }

  function getActivityFocuses(activityId) {

    mainTable.clearSubtableVal();

    $.ajax({
      async : true,
      type : "GET",
      url : prefixUrl + `/portal/rest/smartactivity/stats/activityfocuses/${activityId}/${timeScaleMinutes * 60000}`,
      contentType : "application/json",
      dataType : "json",
      success : successF,
      error : errorF
    });

    function successF(data, textStatus, jqXHR) {

      data.forEach(function (obj) {
        changeObjectPropertyName(obj, "focusChartData", "focus_chart_data");
        changeObjectPropertyName(obj, "activityTitle", "activity_title");
      });

      mainTable.updateSubtableVal(data);

      debug("getActivityFocuses  ajax request success result");
    }

    function errorF(jqXHR, textStatus, errorThrown) {
      log("getActivityFocuses error textStatus:  " + textStatus);
      log("getActivityFocuses errorThrown:  ", errorThrown);
    }
  }

  function closeExpendedRow() {
    $(".v-data-table__expand-icon--active").trigger("click");
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

  function addActiveClassToMenuNavigation() {
    $(".uiUserNavIconAppStats").parent().parent().addClass("active");

    $(".uiUserNavIconAppStats").css("content", "url(/smartactivity/icon/stats-icon25blue.png) !important");
  }

})(jqModule, vuetifyModule, vueModule, eXoVueI18nModule, googleChartsModule);

