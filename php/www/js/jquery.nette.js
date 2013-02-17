/**
 * AJAX Nette Framework plugin for jQuery
 *
 * @copyright   Copyright (c) 2009 Jan Marek
 * @license     MIT
 * @link        http://addons.nette.org/cs/jquery-ajax
 * @version     0.2
 */
var payload;
jQuery.extend({
	nette: {
		updateSnippet: function (id, html) {
			$("#" + id).html(html);
		},

		success: function (payload) {
                        // empty payload
                        // 
			// redirect
			if (payload.redirect) {
				window.location.href = payload.redirect;
				return;
			}

			// snippets
			if (payload.snippets) {
				for (var i in payload.snippets) {
					jQuery.nette.updateSnippet(i, payload.snippets[i]);
				}
			}
		}
	}
});

jQuery.ajaxSetup({
	success: jQuery.nette.success,
	dataType: "json"
});

// odesílání odkazů
$('a.ajax').live('click', function (event) {
	event.preventDefault();
	$.get(this.href);
});

// odesílání formulářů
$('form.ajax').live('submit', function (event) {
	event.preventDefault();
	$.post(this.action, $(this).serialize());
});

$(document).ready(function () {
    $.fn.extend({
        triggerAndReturn: function (name, data) {
            var event = new $.Event(name);
            this.trigger(event, data);
            return event.result !== false;
        }
    });

   $('a[data-confirm], button[data-confirm], input[data-confirm]').live('click', function (e) {
        var el = $(this);
        if (el.triggerAndReturn('confirm')) {
            if (!confirm(el.attr('data-confirm'))) {
                return false;
            }
        }
    });
});
