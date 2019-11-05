(function ($, vuetify, vue, eXoVueI18n) {

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


  appSmartactivityTableVueAndVuetifySubtableValues = [
    {
      startTimeStatistics : 245,
      stopTimeStatistics : 4.0,
      totalFocusStatistics : 5,
      contentFocusStatistics : 7545,
      convoFocusStatistics : 456,
      contentHitsStatistics : 0,
      convoHitsStatistics : 5,
      appHitsStatistics : 7,
      profileHitsStatistics : 5,
      linkHitsStatistics : 1,
    },
    {
      startTimeStatistics : 57,
      stopTimeStatistics : 68.0,
      totalFocusStatistics : 79,
      contentFocusStatistics : 4,
      convoFocusStatistics : 7,
      contentHitsStatistics : 0,
      convoHitsStatistics : 786,
      appHitsStatistics : 7,
      profileHitsStatistics : 578,
      linkHitsStatistics : 0,
    },
    {
      startTimeStatistics : 456,
      stopTimeStatistics : 0.0,
      totalFocusStatistics : 87,
      contentFocusStatistics : 333,
      convoFocusStatistics : 7,
      contentHitsStatistics : 76,
      convoHitsStatistics : 57689,
      appHitsStatistics : 35,
      profileHitsStatistics : 25,
      linkHitsStatistics : 57857,
    },
  ];

  var mainTable;

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
            {
              text : 'Activity Data (Title)',
              align : 'left',
              sortable : false,
              value : 'activityTitle',
            },
            {text : 'Created', value : 'activityCreated'},
            {text : 'Updated', value : 'activityUpdated'},
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
            {text : '', value : 'data-table-expand'},
          ],
          tableVal : tableValues,
        }
      },
      methods : {
        selectTableRow : function (event) {
          if (event.value == true) {
            var activityTitle = event.item.activityTitle;

            console.log(event);
            appSmartactivityTableVueAndVuetifySubtableValues.forEach(function (elem) {
              ++elem.linkHitsStatistics;
            });
          }
        },
        getDataForTheTable : function (event) {
          getUserFocuses(streamSelected, substreamSelected);
        },
        updateTableVal : function (newData) {
          this.tableVal.splice(0, this.tableVal.length);

          for (var index in newData) {
            this.tableVal.push(newData[index]);
          }
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

})(jqModule, vuetifyModule, vueModule, eXoVueI18nModule);

