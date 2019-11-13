(function ($, vuetify, vue, eXoVueI18n, googleCharts) {

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

  google.charts.load('current', {'packages' : ['corechart']});


  var prefixUrl = pageBaseUrl(location);

  var streamSelected = 'All streams';
  var substreamSelected = null;
  var streamPointSelectedData = [];

  var streamSettingsVars = [
    'All streams',
    'Space',
    'User',
  ];

  var timeScaleValue = 3;
  var timeScaleMinutes = 60;

  var timeScaleSettingsVars = [
    '10 min',
    '20 min',
    '30 min',
    '1 hour',
    '4 hours',
    '1 day',
    '1 week',
  ];

  var tableValues = [];

  var focusChartDatesAndValues = [['2013', '684'],
    ['2014', '721'],
    ['2015', '816'],
    ['2016', '932'],
    ['2016', '546'],
    ['2016', '758'],
  ];

  var mainTable;

  var lastOpenedMainTableRow;

  //values for subtable scaling
  var activitiesStreamPrettyIdMaxString = "";
  var activitiesTitleMaxString = "";

  $(document).ready(function () {

    console.log("prefixUrl: " + prefixUrl);

    defineMainTable();

    defineTimeScaler();

    defineStreamSelector();

    console.log("smartactivity-admin.js and page ready!");
  });

  function defineMainTable() {
    mainTable = new Vue({
      el : '#app-smartactivity-table-vue-and-vuetify',
      vuetify : new Vuetify(),
      data() {
        return {
          expanded : [],
          search : '',
          singleExpand : true,
          headers : [
            {text : '', value : 'data-table-expand'},
            {
              text : 'Activity Data (Title)',
              align : 'left',
              sortable : false,
              value : 'activity_title',
            },
            {text : 'Created', value : 'activity_created'},
            {text : 'Updated', value : 'activity_updated'},
            {text : 'Start Time', value : 'localStartTime'},
            {text : 'Stop Time', value : 'localStopTime'},
            {text : 'Total Focus', value : 'totalShown'},
            {text : 'Content Focus', value : 'contentShown'},
            {text : 'Convo Focus', value : 'convoShown'},
            {text : 'Content Hits', value : 'contentHits'},
            {text : 'Convo Hits', value : 'convoHits'},
            {text : 'App Hits', value : 'appHits'},
            {text : 'Profile Hits', value : 'profileHits'},
            {text : 'Link Hits', value : 'linkHits'},
            {text : 'Total Focus Chart', value : 'focus_chart_data'},
          ],
          tableVal : tableValues,
          subtableVal : [],
        }
      },
      methods : {
        selectTableRow : function (event) {
          if (event.value == true) {
            var activityTitle = event.item.activityTitle;

            lastOpenedMainTableRow = event.item;

            getActivityFocuses(lastOpenedMainTableRow.activityId);
            console.log("Opened row: " + event);
          }
        },
        getDataForTheTable : function (event) {
          getUserFocuses(streamSelected, substreamSelected);
        },
        updateTableVal : function (newData) {
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
        updateSubtableVal : function (newData) {
          this.subtableVal.splice(0, this.subtableVal.length);

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
        },
        drawChart : function (item) {
          var id = 'chart-div-' + item.activityId + '-' + item.startTime;

          //async call
          setTimeout(createChart, 25, id, item);

          return id;
        }
      }
    });
  }

  function defineTimeScaler() {
    new Vue({
      el : '#time-scale',
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
          console.log("Time scale: " + event);

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
          console.log(timeScaleMinutes);
        }
      }
    });
  }

  function defineStreamSelector() {
    new Vue({
      el : '#stream-selector',
      vuetify : new Vuetify(),
      data : () => ({
        stream : streamSelected,
        streams : streamSettingsVars,
      }),
      methods : {
        selectStream : function (event) {
          streamSelected = event;
          console.log("Selected stream: " + event);

          deleteSubstreamSelector();
          streamPointSelectedData.length = 0;

          switch (event) {
            case "All streams":
              substreamSelected = null;
              break;
            case "Space":
              substreamSelected = "All spaces";
              getPointsOfTheSelectedStream("user-space");
              break;
            case "User":
              substreamSelected = "All users";
              getPointsOfTheSelectedStream("user-connection");
              break;
          }
        }
      }
    });
  }

  console.log("smartactivity-admin.js");

  function getPointsOfTheSelectedStream(dataClass) {
    streamPointSelectedData.length = 0;

    $(`.${dataClass}`).each(function (index) {
      streamPointSelectedData.push({dataValue : $(this).text()});
    });

    addSubstreamSelector();
    defineSubstreamSelector();
  }

  function addSubstreamSelector() {
    $("#stream-selector").after(`<div id="substream-selector" class="VuetifyApp">
                <v-app id="substream-selector-app">
                    <div>
                        <v-select v-on:change="selectSubstream" v-model="substream" :items="substreams"></v-select>
                    </div>
                </v-app>
            </div>`);
  }

  function deleteSubstreamSelector() {
    $("#substream-selector").remove();
  }

  function defineSubstreamSelector() {
    var substreamData = [];

    substreamData.push(substreamSelected);
    for (var element in streamPointSelectedData) {
      substreamData.push(streamPointSelectedData[element].dataValue);
    }

    new Vue({
      el : '#substream-selector',
      vuetify : new Vuetify(),
      data : () => ({
        substream : substreamSelected,
        substreams : substreamData,
      }),
      methods : {
        selectSubstream : function (event) {
          substreamSelected = event;
          console.log("Selected substream: " + event);
        }
      }
    });
  }

  function getUserFocuses(stream, substream) {

    $.ajax({
      async : true,
      type : "GET",
      url : prefixUrl + `/portal/rest/smartactivity/stats/userfocus/${stream}/${substream}`,
      contentType : "application/json",
      dataType : 'json',
      success : successF,
      error : errorF
    });

    function successF(data, textStatus, jqXHR) {

      data.forEach(function (obj) {
        changeObjectPropertyName(obj, "focusChartData", "focus_chart_data");
        changeObjectPropertyName(obj, "activityTitle", "activity_title");
      });

      mainTable.updateTableVal(data);


      console.log("ajax data:  " + data);
      console.log("textStatus:  " + textStatus);
      console.log("jqXHR:  " + jqXHR);
    }

    function errorF(jqXHR, textStatus, errorThrown) {
      console.log("error jqXHR:  " + jqXHR);
      console.log("error textStatus:  " + textStatus);
      console.log("error errorThrown:  " + errorThrown);
    }

  }

  function drawChart(focusChartDatesAndValues) {
    var headersAndData = [['Time', 'Focus']];

    if (typeof myVar !== 'undefined') {
      focusChartDatesAndValues.forEach(function (elem) {
        elem[1] = Number(elem[1]);
        headersAndData.push(elem);
      });

      var data = google.visualization.arrayToDataTable(
        headersAndData
      );

      var options = {
        // title : 'Focuses',
        hAxis : {title : 'Time', titleTextStyle : {color : '#333'}},
        vAxis : {minValue : 0}
      };

      var chart = new google.visualization.AreaChart(document.getElementById('chart_div'));
      chart.draw(data, options);
    }
  }

  function changeObjectPropertyName(obj, oldKey, newKey) {
    if (oldKey !== newKey) {
      Object.defineProperty(obj, newKey,
        Object.getOwnPropertyDescriptor(obj, oldKey));
      delete obj[oldKey];
    }
  }

  function createChart(id, item) {

    var chartBlock = $(`#${id}`);

    if (chartBlock.length) {
      var headersAndData = [['Time', 'Focus']];

      var focusChartDatesAndValues = item.focus_chart_data;

      focusChartDatesAndValues.forEach(function (elem) {
        elem[1] = Number(elem[1]);
        headersAndData.push(elem);
      });

      var data = google.visualization.arrayToDataTable(
        headersAndData
      );

      var options = {
          legend : 'none',
          hAxis : {
            allowContainerBoundaryTextCufoff : true,
            textStyle : {color : '#FFF', fontSize : 0},
            showTextEvery : 0,
            tiks : [],
            baseline : {
              color : 'green'
            },
            gridlines : {
              color : '#FFF'
            },
            minorGridlines : {
              color : '#FFF'
            }
          },
          vAxis : {
            minValue : 0,
            textStyle : {
              color : '#FFF'
            },
            gridlines : {
              color : '#FFF'
            },
            minorGridlines : {
              color : '#FFF'
            },
            showTextEvery : 0,
            baseline : {
              color : '#FFF'
            },
          },
          lineWidth : 0,
        }
      ;

      var chart = new google.visualization.AreaChart(document.getElementById(id));
      chart.draw(data, options);

      chartBlock.parent().parent().css("height", "max-content");
    }
  }

  function getActivityFocuses(activityId) {

    $.ajax({
      async : true,
      type : "GET",
      url : prefixUrl + `/portal/rest/smartactivity/stats/activityfocuses/${activityId}/${timeScaleMinutes * 60000}`,
      contentType : "application/json",
      dataType : 'json',
      success : successF,
      error : errorF
    });

    function successF(data, textStatus, jqXHR) {

      //copyActivityDataToActivityFocuses(data);

      data.forEach(function (obj) {
        changeObjectPropertyName(obj, "focusChartData", "focus_chart_data");
        changeObjectPropertyName(obj, "activityTitle", "activity_title");
      });

      mainTable.updateSubtableVal(data);

      console.log("ajax data:  " + data);
      console.log("textStatus:  " + textStatus);
      console.log("jqXHR:  " + jqXHR);
    }

    function errorF(jqXHR, textStatus, errorThrown) {
      console.log("error jqXHR:  " + jqXHR);
      console.log("error textStatus:  " + textStatus);
      console.log("error errorThrown:  " + errorThrown);
    }


  }
})(jqModule, vuetifyModule, vueModule, eXoVueI18nModule, googleChartsModule);

