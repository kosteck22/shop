var buttonLoad;
var dropDownCountry;
var buttonAddCountry;
var buttonUpdateCountry;
var buttonDeleteCountry;
var labelCountryName;
var fieldCountryName;
var fieldCountryCode;

$(document).ready(function() {
    buttonLoad = $("#buttonLoadCountries");
    dropDownCountry = $("#dropDownCountries");
    buttonAddCountry = $("#buttonAddCountry");
    buttonUpdateCountry = $("#buttonUpdateCountry");
    buttonDeleteCountry = $("#buttonDeleteCuun");
    labelCountryName = $("#labelCountryName")
    fieldCountryName = $("#fieldCountryName");
    fieldCountryCode = $("#fieldCountryCode");

   buttonLoad.click(function() {
    loadCountries();
   });

   dropDownCountry.on("click", function() {
    changeFormStateToSelectedCountry();
   });

   buttonAddCountry.click(function() {
    if (buttonAddCountry.val() == "Add") {
        addCountry();
    } else {
        changeFormCountryToNew();
    }
   });

   buttonUpdateCountry.click(function() {
    updateCountry();
   });

   buttonDeleteCountry.click(function() {
    deleteCountry();
   });
});

function deleteCountry() {
    optionValue = dropDownCountry.val();
    countryId = optionValue.split("-")[0];

    url = contextPath + "countries/delete/" + countryId;

    $.ajax({
            type: 'DELETE',
            url: url,
            beforeSend: function(xhr) {
                xhr.setRequestHeader(csrfHeaderName, csrfValue);
            }
    }).done(function(countryId) {
        $("#dropDownCountries option[value='" + optionValue + "']").remove();
        changeFormCountryToNew();
        alert("The Country has been deleted");
    });
}

function validateFormCountry() {
    formCountry = document.getElementById("formCountry");
    if (!formCountry.checkValidity()) {
        formCountry.reportValidity();
        return false;
    }
    return true;
}

function updateCountry() {
    if (!validateFormCountry()) return;

    url = contextPath + "countries/save";
        countryName = fieldCountryName.val();
        countryCode = fieldCountryCode.val();
        countryId = dropDownCountry.val().split("-")[0];
        jsonData = {id: countryId, name: countryName, code: countryCode};

        $.ajax({
            type: 'POST',
            url: url,
            beforeSend: function(xhr) {
                xhr.setRequestHeader(csrfHeaderName, csrfValue);
            },
            data: JSON.stringify(jsonData),
            contentType: 'application/json'
        }).done(function(countryId) {
            $("#dropDownCountries option:selected").val(countryId + "-" + countryCode);
            $("#dropDownCountries option:selected").text(countryName);

            changeFormCountryToNew();
            alert("Country updated");
        });
}

function addCountry() {
    if (!validateFormCountry()) return;

    url = contextPath + "countries/save";
    countryName = fieldCountryName.val();
    countryCode = fieldCountryCode.val();
    jsonData = {name: countryName, code: countryCode};

    $.ajax({
        type: 'POST',
        url: url,
        beforeSend: function(xhr) {
            xhr.setRequestHeader(csrfHeaderName, csrfValue);
        },
        data: JSON.stringify(jsonData),
        contentType: 'application/json'
    }).done(function(countryId) {
        selectNewlyAddedCountry(countryId, countryCode, countryName);
        alert("Country added");
    });
}

function selectNewlyAddedCountry(countryId, countryCode, countryName) {
    optionValue = countryId + "-" + countryCode;
    $("<option>").val(optionValue).text(countryName).appendTo(dropDownCountry);

    $("#dropDownCountries option[value='" + optionValue + "']").prop("selected", true);
    fieldCountryCode.val("");
    fieldCountryName.val("").focus();
}

function changeFormCountryToNew() {
    buttonAddCountry.val("Add");
    labelCountryName.text("Country Name:");

    buttonUpdateCountry.prop("disabled", true);
    buttonDeleteCountry.prop("disabled", true);

    fieldCountryName.val("");
    fieldCountryCode.val("").focus();
}

function changeFormStateToSelectedCountry() {
    buttonAddCountry.prop("value", "New");
    buttonUpdateCountry.prop("disabled", false);
    buttonDeleteCountry.prop("disabled", false);

    selectedCountryName = $("#dropDownCountries option:selected").text();
    fieldCountryName.val(selectedCountryName);
    labelCountryName.text("Selected Country:");

    countryCode = dropDownCountry.val().split("-")[1];
    fieldCountryCode.val(countryCode);
}

function loadCountries() {
    url = contextPath + "countries/list";
    $.get(url, function(responseJSON) {
        dropDownCountry.empty();

        $.each(responseJSON, function(index, country) {
            optionValue = country.id + "-" + country.code;
            $("<option>").val(optionValue).text(country.name).appendTo(dropDownCountry);
        });

    }).done(function() {
        buttonLoad.val("Refresh Country List");
    });
}