    function showModalDialog(title, message) {
    $("#modalTitle").text(title);
    $("#modalBody").text(message);
    $("#modalDialog").modal();
    var myModal = new bootstrap.Modal(document.getElementById("modalDialog"), {});
    myModal.show();
  }