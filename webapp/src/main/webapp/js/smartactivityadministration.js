(function ($, vuetify, vue, eXoVueI18n) {

  // ******** REST services ********

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
  var streamPointSelectedData = null;

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


  var tableValues = [
    {
      activityTitle : 'My Activity 11',
      activityCreated : 159,
      activityUpdated : 6.0,
      startTimeStatistics : 24,
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
      activityTitle : 'My Activity 2',
      activityCreated : 57,
      activityUpdated : 5785.0,
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
      activityTitle : 'My Activity 3',
      activityCreated : 587,
      activityUpdated : 87.0,
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


  $(document).ready(function () {

    console.log("prefixUrl: " + prefixUrl);


    new Vue({
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
            {text : 'Activity Statistics (Start Time)', value : 'startTimeStatistics'},
            {text : 'Stop Time', value : 'stopTimeStatistics'},
            {text : 'Total Focus', value : 'totalFocusStatistics'},
            {text : 'Content Focus', value : 'contentFocusStatistics'},
            {text : 'Convo Focus', value : 'convoFocusStatistics'},
            {text : 'Content Hits', value : 'contentHitsStatistics'},
            {text : 'Convo Hits', value : 'convoHitsStatistics'},
            {text : 'App Hits', value : 'appHitsStatistics'},
            {text : 'Profile Hits', value : 'profileHitsStatistics'},
            {text : 'Link Hits', value : 'linkHitsStatistics'},
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

      }
    });


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
          streamPointSelectedData = null;

          switch (event) {
            case "All streams":
              substreamSelected = null;
              break;
            case "Space":
              getPointsOfTheSelectedStream("userspaces");
              break;
            case "User":
              getPointsOfTheSelectedStream("userconnections");
              break;
          }

        }
      }
    });

    console.log("smartactivityadministration.js and page ready!");
  });

  console.log("smartactivityadministration.js");

  function getPointsOfTheSelectedStream(path) {
    $.ajax({
      type : "POST",
      url : prefixUrl + `/portal/rest/smartactivity/stats/${path}`,
      data : {user : "nick"},
      success : successF,
      error : errorF,
      contentType : "application/json"
    });

    function successF(data, textStatus, jqXHR) {
      console.log(`ajax selected ${streamSelected} data: ` + data);
      streamPointSelectedData = data;

      addSubstreamSelector();
      defineSubstreamSelector();
    }

    function errorF(jqXHR, textStatus, errorThrown) {
      console.log("error jqXHR:  " + jqXHR);
      console.log("error textStatus:  " + textStatus);
      console.log("error errorThrown:  " + errorThrown);
    }
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

    switch (streamSelected) {
      case "Space":
        substreamSelected = "All spaces";

        substreamData.push(substreamSelected);
        for (var element in streamPointSelectedData) {
          substreamData.push(streamPointSelectedData[element].displayName);
        }
        break;
      case "User":
        substreamSelected = "All users";

        substreamData.push(substreamSelected);
        for (var element in streamPointSelectedData) {
          substreamData.push(streamPointSelectedData[element].userFullName);
        }
        break;
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

  function getUserFocuses(streamSelected, substreamSelected) {

    var dataTransfer = {
      streamselected : streamSelected,
      substreamselected : substreamSelected
    };

    var dataTransferTransformed = $.param(dataTransfer);

    $.ajax({
      type : "POST",
      url : prefixUrl + "/portal/rest/smartactivity/stats/userfocus",
      data : dataTransferTransformed,
      success : successF,
      error : errorF,
      contentType : "application/json"
    });

    function successF(data, textStatus, jqXHR) {

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

