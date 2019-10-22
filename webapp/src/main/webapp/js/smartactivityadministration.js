(function ($, vuetify, vue, eXoVueI18n) {
    var streamSelected = 'All streams';

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
            activityTitle: 'My Activity 1',
            activityCreated: 159,
            activityUpdated: 6.0,
            startTimeStatistics: 24,
            stopTimeStatistics: 4.0,
            totalFocusStatistics: 5,
            contentFocusStatistics: 7545,
            convoFocusStatistics: 456,
            contentHitsStatistics: 0,
            convoHitsStatistics: 5,
            appHitsStatistics: 7,
            profileHitsStatistics: 5,
            linkHitsStatistics: 1,
        },
        {
            activityTitle: 'My Activity 2',
            activityCreated: 57,
            activityUpdated: 5785.0,
            startTimeStatistics: 57,
            stopTimeStatistics: 68.0,
            totalFocusStatistics: 79,
            contentFocusStatistics: 4,
            convoFocusStatistics: 7,
            contentHitsStatistics: 0,
            convoHitsStatistics: 786,
            appHitsStatistics: 7,
            profileHitsStatistics: 578,
            linkHitsStatistics: 0,
        },
        {
            activityTitle: 'My Activity 3',
            activityCreated: 587,
            activityUpdated: 87.0,
            startTimeStatistics: 456,
            stopTimeStatistics: 0.0,
            totalFocusStatistics: 87,
            contentFocusStatistics: 333,
            convoFocusStatistics: 7,
            contentHitsStatistics: 76,
            convoHitsStatistics: 57689,
            appHitsStatistics: 35,
            profileHitsStatistics: 25,
            linkHitsStatistics: 57857,
        },
    ];


    var subtableValues = [
        {
            startTimeStatistics: 245,
            stopTimeStatistics: 4.0,
            totalFocusStatistics: 5,
            contentFocusStatistics: 7545,
            convoFocusStatistics: 456,
            contentHitsStatistics: 0,
            convoHitsStatistics: 5,
            appHitsStatistics: 7,
            profileHitsStatistics: 5,
            linkHitsStatistics: 1,
        },
        {
            startTimeStatistics: 57,
            stopTimeStatistics: 68.0,
            totalFocusStatistics: 79,
            contentFocusStatistics: 4,
            convoFocusStatistics: 7,
            contentHitsStatistics: 0,
            convoHitsStatistics: 786,
            appHitsStatistics: 7,
            profileHitsStatistics: 578,
            linkHitsStatistics: 0,
        },
        {
            startTimeStatistics: 456,
            stopTimeStatistics: 0.0,
            totalFocusStatistics: 87,
            contentFocusStatistics: 333,
            convoFocusStatistics: 7,
            contentHitsStatistics: 76,
            convoHitsStatistics: 57689,
            appHitsStatistics: 35,
            profileHitsStatistics: 25,
            linkHitsStatistics: 57857,
        },
    ];


    $(document).ready(function () {
        new Vue({
            el: '#app-smartactivity-table-vue-and-vuetify',
            vuetify: new Vuetify(),
            data() {
                return {
                    headers: [
                        {
                            text: '',
                            align: 'left',
                            sortable: false,
                            value: 'arrow',
                        },
                        {
                            text: 'Activity Data (Title)',
                            value: 'activityTitle',
                        },
                        {text: 'Created', value: 'activityCreated'},
                        {text: 'Updated', value: 'activityUpdated'},
                        {text: 'Activity Statistics (Start Time)', value: 'startTimeStatistics'},
                        {text: 'Stop Time', value: 'stopTimeStatistics'},
                        {text: 'Total Focus', value: 'totalFocusStatistics'},
                        {text: 'Content Focus', value: 'contentFocusStatistics'},
                        {text: 'Convo Focus', value: 'convoFocusStatistics'},
                        {text: 'Content Hits', value: 'contentHitsStatistics'},
                        {text: 'Convo Hits', value: 'convoHitsStatistics'},
                        {text: 'App Hits', value: 'appHitsStatistics'},
                        {text: 'Profile Hits', value: 'profileHitsStatistics'},
                        {text: 'Link Hits', value: 'linkHitsStatistics'},
                    ],
                    tableVal: tableValues,
                }
            },
            methods: {
                selectTableRow: function (event) {
                    console.log(event);

                    var selectedRowElem = $(`#app-smartactivity-table-vue-and-vuetify td:contains(${event.activityTitle})`).parent();

                    console.log("selectedRowElem: " + selectedRowElem);

                    //mobile row element
                    if (selectedRowElem.length == 0) {
                        selectedRowElem = $(`#app-smartactivity-table-vue-and-vuetify .v-data-table__mobile-row__wrapper:contains(${event.activityTitle})`)
                            .parent().parent();
                        console.log(selectedRowElem);
                    }

                    //is it selected
                    if (selectedRowElem.css("background-color") == "rgb(211, 211, 211)") {
                        //cancel select
                        this.cleanTable();
                    } else {
                        //delete selected before and select the new row
                        this.cleanTable();

                        selectedRowElem.css("background-color", "rgb(211, 211, 211)");

                        var arrow = '<i role="button" class="open-row-arrow v-icon notranslate ' +
                            'v-treeview-node__toggle v-icon--link mdi mdi-menu-down theme--light v-treeview-node__toggle--open"></i>';

                        // add an arrow to the open row
                        $(`#app-smartactivity-table-vue-and-vuetify td:contains(${event.activityTitle})`)
                            .parent().find("td").first().html(arrow);

                        // add an arrow to the open row (mobile)
                        // $( `.v-data-table__mobile-row__cell:contains(${event.activityTitle})` ).append( arrow );

                        //add subtable
                        selectedRowElem.after(
                            `
                            <div id="subtable">
                              <v-app id="subtable-app" >
                                <v-data-table
                                  :headers="headers"
                                  :items="values"
                                  :items-per-page="100"
                                  sort-by="startTimeStatistics"
                                  class="elevation-2 grey lighten-4"
                                ></v-data-table>
                              </v-app>
                            </div>
                            `);

                        setupOpendTable();
                        horizontalTableScrollSynchronizationAdd();
                    }
                },
                cleanTable: function (event) {
                    //clean a row background color
                    $(`.text-left`).parent().css("background-color", "");
                    //clean a row background color (mobile)
                    $(`.v-data-table__mobile-row__wrapper`).parent().parent().css("background-color", "");

                    //delete opened subtable
                    $("#subtable").remove();

                    addArrows();
                },
            }
        });

        addArrows();


        new Vue({
            el: '#time-scale',
            vuetify: new Vuetify(),
            data() {
                return {
                    value: timeScaleValue,
                    timeScaleModel: 3,
                    ticksLabels: timeScaleSettingsVars,
                }
            },
            methods: {
                selectTimeScale: function (event) {
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
            el: '#stream-selector',
            vuetify: new Vuetify(),
            data: () => ({
                stream: streamSelected,
                streams: streamSettingsVars,
            }),
            methods: {
                selectStream: function (event) {
                    console.log("Selected stream: " + event);
                }
            }
        });

        console.log("smartactivityadministration.js and page ready!");
    });

    console.log("smartactivityadministration.js");

    function setupOpendTable() {
        new Vue({
            el: '#subtable',
            vuetify: new Vuetify(),
            data() {
                return {
                    headers: [
                        {
                            text: '',
                            align: 'left',
                            sortable: false,
                            value: 'arrow',
                        },
                        {
                            text: 'Activity Data (Title)',
                            sortable: false,
                            value: 'activityTitle',
                        },
                        {text: 'Created', value: 'activityCreated'},
                        {text: 'Updated', value: 'activityUpdated'},
                        {text: 'Activity Statistics (Start Time)', value: 'startTimeStatistics'},
                        {text: 'Stop Time', value: 'stopTimeStatistics'},
                        {text: 'Total Focus', value: 'totalFocusStatistics'},
                        {text: 'Content Focus', value: 'contentFocusStatistics'},
                        {text: 'Convo Focus', value: 'convoFocusStatistics'},
                        {text: 'Content Hits', value: 'contentHitsStatistics'},
                        {text: 'Convo Hits', value: 'convoHitsStatistics'},
                        {text: 'App Hits', value: 'appHitsStatistics'},
                        {text: 'Profile Hits', value: 'profileHitsStatistics'},
                        {text: 'Link Hits', value: 'linkHitsStatistics'},
                    ],
                    values: subtableValues,
                }
            },
        });

        // add shift empty div to subtable
        $("#subtable .text-left").html("<div class='shift-div'></div>");
    }

    function horizontalTableScrollSynchronizationAdd() {
        $('#subtable .v-data-table__wrapper').first().scroll(function () {
            $('#app-smartactivity-table-vue-and-vuetify .v-data-table__wrapper').first().scrollLeft($(this).scrollLeft());
        });
    }

    function addArrows() {
        var arrow = '<i role="button" class="v-icon notranslate v-treeview-node__toggle ' +
            'v-icon--link mdi mdi-menu-down theme--light"></i>';

        //pc
        $("#app-smartactivity-table-vue-and-vuetify tr .text-left").html(arrow);

        // mobile
        $(".open-row-arrow").parent().html(arrow);

        //clean title
        $("#app-smartactivity-table-vue-and-vuetify tr .text-left").first().html(' ');
    }

})(jqModule, vuetifyModule, vueModule, eXoVueI18nModule);

