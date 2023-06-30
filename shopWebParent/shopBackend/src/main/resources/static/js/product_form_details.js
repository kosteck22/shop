 $(document).ready(function() {
    $("a[name='linkRemoveDetail']").each(function(index) {
        $(this).click(function() {
            removeDetailSectionByIndex(index);
        });
    });
  });

function addNextDetailSection() {
    allDivDetails = $("[id^='divDetail']");
    divDetailsCount = allDivDetails.length;

    htmlDetailSection = `
      <div class="input-group p-2" id="divDetail${divDetailsCount}">
          <input type="hidden" name="detailIds" value="0">
          <span class="input-group-text">Name:</span>
          <input type="text" class="form-control" name="detailNames" maxlength="255">
          <span class="input-group-text">Value:</span>
          <input type="text" class="form-control" name="detailValues" maxlength="255">
        </div>
    `;

    $("#divProductDetails").append(htmlDetailSection);

    previousDivDetailSection = allDivDetails.last();
    previousDivDetailId = previousDivDetailSection.attr("id");

    htmlLinkRemove = `
            <a class="btn fas fa-times-circle fa-2x icon-dark"
            href="javascript:removeDetailSectionById('${previousDivDetailId}')"
            title="Remove this detail"></a>
        `;

    previousDivDetailSection.append(htmlLinkRemove);

    $("input[name='detailNames']").last().focus();
}

function removeDetailSectionById(id) {
    $("#" + id).remove();
}

function removeDetailSectionByIndex(index) {
    $("#divDetail" + index).remove();
}