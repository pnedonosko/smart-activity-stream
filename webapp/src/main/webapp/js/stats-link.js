$(document).ready(function () {
  var blockWithLocalization = $("#stats-link");

  if (blockWithLocalization.length) {
    //async call
    setTimeout(addStatsLinkToProfileMenuNav, 1);

    //async call
    setTimeout(addStatsLinkToDropdownUserMenu, 200);
  }

});

function addStatsLinkToDropdownUserMenu() {
  var statsMenuLinkTitle = $("#stats-menu-link-title").text();

  $("#UIUserPlatformToolBarPortlet .uiIconAppGamification").parent().parent().after(`
  <li>
    <a href="/portal/intranet/stats">
      <i class="uiIconAppStats"></i>${statsMenuLinkTitle}
    </a>
  </li>
  `);
}

function addStatsLinkToProfileMenuNav() {
  var statsMenuLinkTitle = $("#stats-menu-link-title").text();

  $(".userNavigation").append(`
  <li class="item">
    <a href="/portal/intranet/stats">
      <div class="uiUserNavIconAppStats uiIconDefaultApp"></div>
      <span class="tabName">${statsMenuLinkTitle}</span>
    </a>
  </li>
  `);
}
