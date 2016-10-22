$.extend({
	getUrlVars : function() {
		var vars = [], hash;
		var hashes = window.location.href.slice(
				window.location.href.indexOf('?') + 1).split('&');
		for (var i = 0; i < hashes.length; i++) {
			hash = hashes[i].split('=');
			vars.push(hash[0]);
			vars[hash[0]] = hash[1];
		}
		return vars;
	},
	getUrlVar : function(name) {
		return $.getUrlVars()[name];
	}
});

function initList(collection, addRowCallback) {

	$.ajax({
		url : "" + collection,
		method : "GET",
		success : function(data) {

			var svnBase = "";
			$.ajax({
				url : "actions/properties/list",
				method : "GET",
				async : false,
				success : function(data) {
					svnBase = data['svnBase'];
				}
			});
			for (var i = 0; i < data._embedded[collection].length; i++) {
				var row = $("thead #model_row").clone();
				$(row).removeClass("hidden");
				$(row).attr("id", "");
				$(row).find("a#edit-link").attr(
						"href",
						"upsert.html?id="
								+ data._embedded[collection][i].id);
				$(row).find("a#download-link").attr(
						"href",
						svnBase + data._embedded[collection][i].project + "/"
								+ data._embedded[collection][i].csvName);
				$(row).find("a#delete-link").data("id",
						data._embedded[collection][i].id).on(
						"click",
						function(e) {
							$.ajax({
								url : "" + collection + "/"
										+ $(this).data("id"),
								method : "DELETE",
								success : listRedirectCallback
							});
						});

				addRowCallback(row, data._embedded[collection][i]);
				$("#table_list").append(row);
			}
		}
	});

}

function getUpsertId() {
	var id = $.trim($("#id").val());
	if ($.trim($("#id").val()).length <= 0)
		id = "0";
	return id;
}

function getUpsertMethod() {
	return ($.trim($("#id").val()).length > 0) ? "PUT" : "POST";
}

function getEntity(collection, id, entityCallback) {
	$.ajax({
		url : "" + collection + "/" + id,
		contentType : "application/json",
		method : "GET",
		success : entityCallback
	});
}

function listRedirectCallback(e) {
	window.location.replace("list.html");
}

function sqlBlackList(code) {
	// TODO: The matching recognizes the blacklisted words as part of other
	// words, and gets false positives.
	// var blacklist = [ "update", "insert", "drop", "delete", "grant" ];
	var blacklist = [];
	for (var i = 0; i < blacklist.length; i++) {
		if (code.indexOf(blacklist[i]) !== -1)
			return blacklist[i];
	}
	return null;
}

function validateUserInput() {

	if ($.trim($("#name").val()).length == 0) {
		alert("The name field cannot be empty");
		return false;
	}
	if ($.trim($("#project").val()).length == 0) {
		alert("The project field cannot be empty");
		return false;
	}
	if ($.trim($("#csvName").val()).length == 0) {
		alert("The csv name field cannot be empty");
		return false;
	}
	var code = ace.edit("code").getValue();
	if ($.trim(code).length == 0) {
		alert("The code field cannot be empty");
		return false;
	}
	var blacklisted = sqlBlackList(code);
	if (blacklisted != null) {
		alert("Your Message Contains the word '" + blacklisted
				+ "' which is restricted. Please remove it before proceeding");
		return false;
	}

	return true;
}

function initUpsert(coll, getEntityCallback) {
	// set the ACE editor for the code field
	var editor = ace.edit("code");
	editor.setTheme("ace/theme/eclipse");
	editor.getSession().setMode("ace/mode/sql");

	var id = $.getUrlVar('id');
	var url = "" + coll;
	if (id != null) {
		getEntity(coll, id, getEntityCallback);
		url = url + "/" + id;
	}

	$("#upsert_button").on("click", function(e) {
		if (validateUserInput()) {
			var obj = getUpsertJSON();
			$.ajax({
				url : url,
				contentType : "application/json",
				data : obj,
				method : getUpsertMethod(),
				success : listRedirectCallback,
				error : function(data) {
					alert(data.responseJSON.message);
				}
			});
		}
	});
}

function alertInfo(text) {
	return "<div id='alert-text' class='alert alert-info fade in alert-dismissible row' role='alert'>"
			+ text + "<i class='cog fa fa-cog faa-spin animated'></i></div>";
}

function alertSuccess(text) {
	return "<div id='alert-text' class='alert alert-success fade in alert-dismissible row' role='alert'>"
			+ text
			+ "<button type='button' class='close' data-dismiss='alert' aria-label='Close'>&times;</button></div>";
}
function alertDanger(text) {
	return "<div id='alert-text' class='alert alert-danger fade in alert-dismissible row' role='alert'>"
			+ text
			+ "<button type='button' class='close' data-dismiss='alert' aria-label='Close'>&times;</button></div>";
}