$(document).ready(function () {
  //The hidden localization block
  var blockWithLocalization = $("#stats-link");

  //If the hidden localization block exists
  if (blockWithLocalization.length) {
    var statsMenuLinkTitle = $("#stats-menu-link-title").text();

    //Async call
    setTimeout(addStatsLinkToProfileMenuNav, 1, statsMenuLinkTitle);

    //Async call
    setTimeout(addStatsLinkToDropdownUserMenu, 200, statsMenuLinkTitle);
  }
});

function addStatsLinkToDropdownUserMenu(statsMenuLinkTitle) {
  $("#UIUserPlatformToolBarPortlet .uiIconAppGamification").parent().parent().after(`
  <li>
    <a href="/portal/intranet/stats">
      <i class="uiIconAppStats"></i>${statsMenuLinkTitle}
    </a>
  </li>
  `);
}

function addStatsLinkToProfileMenuNav(statsMenuLinkTitle) {
  $(".userNavigation").append(`
  <li class="item">
    <a href="/portal/intranet/stats">
      <div class="uiUserNavIconAppStats uiIconDefaultApp"></div>
      <span class="tabName">${statsMenuLinkTitle}</span>
    </a>
  </li>
  `);
}
