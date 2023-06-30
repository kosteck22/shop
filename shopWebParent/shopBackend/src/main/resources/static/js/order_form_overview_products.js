var fieldProductCost;
var fieldSubtotal;
var fieldShippingCost;
var fieldTax;
var fieldTotal;

$(document).ready(function() {
    fieldProductCost = $("#productCost");
    fieldSubtotal = $("#subtotal");
    fieldShippingCost = $("#shippingCost");
    fieldTax = $("#tax");
    fieldTotal = $("#total");


    formatOrderAmounts();
    formatProductAmounts();

    $("#productList").on("change", ".quantity-input", function(e) {
        updateSubtotalWhenQuantityChanged($(this));
        updateOrderAmounts();
    });

    $("#productList").on("change", ".price-input", function(e) {
            updateSubtotalWhenPriceChanged($(this));
            updateOrderAmounts();
    });

    $("#productList").on("change", ".cost-input", function(e) {
            updateOrderAmounts();
    });

    $("#productList").on("change", ".ship-input", function(e) {
            updateOrderAmounts();
    });
});

function updateOrderAmounts() {
    totalCost = parseFloat(0.0);

    $(".cost-input").each(function(e) {
        costInputField = $(this);
        rowNumber = costInputField.attr("rowNumber");
        quantityValue = $("#quantity" + rowNumber).val();

        productCost = costInputField.val();
        totalCost += parseFloat(productCost * quantityValue);
    });

    setAndFormatNumberForField("productCost", totalCost);

    orderSubtotal = parseFloat(0.0);

    $(".subtotal-output").each(function(e) {
        productSubtotal = $(this).val();
        orderSubtotal += parseFloat(productSubtotal);
    });

    setAndFormatNumberForField("subtotal", orderSubtotal);

    shippingCost = parseFloat(0.0);

    $(".ship-input").each(function(e) {
        productShip = $(this).val();
        shippingCost += parseFloat(productShip);
    });

    setAndFormatNumberForField("shippingCost", shippingCost);

    tax = parseFloat(fieldTax.val());
    orderTotal = parseFloat(orderSubtotal + tax + shippingCost);
    console.log(orderTotal);
    setAndFormatNumberForField("total", orderTotal);
}

function setAndFormatNumberForField(fieldId, fieldValue) {
    $("#" + fieldId).val(parseFloat(fieldValue).toFixed(2));
}

function updateSubtotalWhenPriceChanged(input) {
        priceValue = input.val();
        rowNumber = input.attr("rowNumber");

        quantityField = $("#quantity" + rowNumber);
        quantityValue = quantityField.val();
        newSubtotal = parseFloat(quantityValue * priceValue);

        setAndFormatNumberForField(("subtotal" + rowNumber), newSubtotal);
}

function updateSubtotalWhenQuantityChanged(input) {
    quantityValue = input.val();
    rowNumber = input.attr("rowNumber");
    priceField = $("#price" + rowNumber);
    priceValue = priceField.val();
    newSubtotal = parseFloat(quantityValue * priceValue);

    setAndFormatNumberForField("subtotal" + rowNumber, newSubtotal);
}

function formatProductAmounts() {
    $(".cost-input").each(function(e) {
        formatNumberForField($(this));
    });

    $(".price-input").each(function(e) {
        formatNumberForField($(this));
    });

    $(".subtotal-output").each(function(e) {
        formatNumberForField($(this));
    });

    $(".ship-input").each(function(e) {
        formatNumberForField($(this));
    });
}

function formatOrderAmounts() {
    formatNumberForField(fieldProductCost);
    formatNumberForField(fieldSubtotal);
    formatNumberForField(fieldShippingCost);
    formatNumberForField(fieldTax);
    formatNumberForField(fieldTotal);
}

function formatNumberForField(fieldRef) {
    fieldRef.val(parseFloat(fieldRef.val()).toFixed(2));
}

function processFormBeforeSubmit() {
    setCountryName();
}

function setCountryName() {
    selectedCountry = $("#country option:selected");
    countryName = selectedCountry.text();
    console.log(countryName.trim());
    $("#countryName").val(countryName.trim());
    console.log($("#countryName").val());
}
