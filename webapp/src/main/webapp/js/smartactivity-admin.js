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

  var customDateFormat = "#DD#/#MM#/#YYYY# #hh#:#mm#:#ss#";

  //customFormat license:
  //*** This code is copyright 2002-2016 by Gavin Kistner, !@phrogz.net
  //*** It is covered under the license viewable at http://phrogz.net/JS/_ReuseLicense.txt
  //*** Reuse or modification is free provided you abide by the terms of that license.
  //*** (Including the first two lines above in your source code satisfies the conditions.)
  Date.prototype.customFormat = function (formatString) {
    var YYYY, YY, MMMM, MMM, MM, M, DDDD, DDD, DD, D, hhhh, hhh, hh, h, mm, m, ss, s, ampm, AMPM, dMod, th;
    var dateObject = this;
    YY = ((YYYY = dateObject.getFullYear()) + "").slice(-2);
    MM = (M = dateObject.getMonth() + 1) < 10 ? ('0' + M) : M;
    MMM = (MMMM = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"][M - 1]).substring(0, 3);
    DD = (D = dateObject.getDate()) < 10 ? ('0' + D) : D;
    DDD = (DDDD = ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"][dateObject.getDay()]).substring(0, 3);
    th = (D >= 10 && D <= 20) ? 'th' : ((dMod = D % 10) == 1) ? 'st' : (dMod == 2) ? 'nd' : (dMod == 3) ? 'rd' : 'th';
    formatString = formatString.replace("#YYYY#", YYYY).replace("#YY#", YY).replace("#MMMM#", MMMM).replace("#MMM#", MMM).replace("#MM#", MM).replace("#M#", M).replace("#DDDD#", DDDD).replace("#DDD#", DDD).replace("#DD#", DD).replace("#D#", D).replace("#th#", th);

    h = (hhh = dateObject.getHours());
    if (h == 0) h = 24;
    if (h > 12) h -= 12;
    hh = h < 10 ? ('0' + h) : h;
    hhhh = hhh < 10 ? ('0' + hhh) : hhh;
    AMPM = (ampm = hhh < 12 ? 'am' : 'pm').toUpperCase();
    mm = (m = dateObject.getMinutes()) < 10 ? ('0' + m) : m;
    ss = (s = dateObject.getSeconds()) < 10 ? ('0' + s) : s;
    return formatString.replace("#hhhh#", hhhh).replace("#hhh#", hhh).replace("#hh#", hh).replace("#h#", h).replace("#mm#", mm).replace("#m#", m).replace("#ss#", ss).replace("#s#", s).replace("#ampm#", ampm).replace("#AMPM#", AMPM);
  };

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
        updateTableVal : function (newData) {
          this.tableVal.splice(0, this.tableVal.length);

          for (var index in newData) {
            newData[index].activityCreated = millisecondsToCustomDate(newData[index].activityCreated, customDateFormat);
            newData[index].activityUpdated = millisecondsToCustomDate(newData[index].activityUpdated, customDateFormat);

            this.tableVal.push(newData[index]);
          }
        }
      }
    });

    function millisecondsToCustomDate(milliseconds, customDateFormat) {
      var customDate = new Date(milliseconds);
      return customDate.customFormat(customDateFormat);
    }
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

  function getUserFocuses(streamSelected, substreamSelected) {

    $.ajax({
      async : true,
      type : "GET",
      url : prefixUrl + `/portal/rest/smartactivity/stats/userfocus/${streamSelected}/${substreamSelected}`,
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

