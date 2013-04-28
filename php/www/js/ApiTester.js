var url;
var post;
var method;

$(document).ready(function() {
    $(".result_p").hide();

    if ($.cookie("fArgs") != undefined)
        $('#args').text($.cookie("fArgs"));
    if ($.cookie("fUrl") != undefined)
        $('#url').val($.cookie("fUrl"));
    if ($.cookie("fMethod") != undefined)
        $('#method').val($.cookie("fMethod"));

    $("#submit").click(buttonClick);
});

function buttonClick() {
    var args = getArgs();
    $.cookie("fArgs", args, {path: '/', expires: 7});

    if (getRelativeUrl() == "") {
        alert("Url must be set");
        return;
    }
    $.cookie("fUrl", getRelativeUrl(), {path: '/', expires: 7});

    url = "/" + getRelativeUrl();
    method = getMethod();
    $.cookie("fMethod", method, {path: '/', expires: 7});
    post = "";
    if (method == "GET") {
        if (args)
            url += "?" + args;
    } else {
        post = args;
    }

    load();
}

function load() {
    $.ajax({
        type: method,
        url: url,
        data: post,
        dataType: "text",
        success: function(data, status, xhr) {
            $(".result_p").show();
            $("#status").text("SUCCESS, code: "+xhr.status+" ("+status+")");
            $("#result").text(data? data : "");
        },
        error: function(xhr, data, status) {
            $(".result_p").show();
            $("#status").text("ERROR, code: "+xhr.status + " ("+status+")");
            $("#result").text(xhr.responseText ? xhr.responseText : "");
        }
    });
}

function getMethod() {
    return $('#method option:selected').text();
}

function getArgs() {
    return $('#args').val();
}

function getRelativeUrl() {
    return $('#url').val();
}
