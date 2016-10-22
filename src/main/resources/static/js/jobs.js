function getDbNames() {
	$("#dbName").empty();
	$.ajax({
		url : "actions/dataSources/list",
		async : false,
		method : "GET",
		success : function(data) {
			$.each(data, function(index, value) {
				$("#dbName").append(
						'<option id =' + value + ' name=' + value + ' value='
								+ value + '>' + value + '</option>');
			});

		}
	});
}

function getUpsertJSON() {
	var data = {};
	data.id = getUpsertId();
	data.name = $.trim($("#name").val());
	data.dbName = $.trim($("#dbName").val());
	data.code = ace.edit("code").getValue();
	data.project = $.trim($("#project").val());
	data.csvName = $.trim($("#csvName").val());
	data.description = $.trim($("#description").val());
	return JSON.stringify(data);
}

function addRow(row, obj) {

	$(row).find("#name").html(obj.name);
	$(row).find("#dbName").html(obj.dbName);
	$(row).find("#project").html(obj.project);
	$(row).find("#csvName").html(obj.csvName);
	$(row).find("#lastExecutionDate").html(
			moment.utc(obj.lastExecutionDate).fromNow());
	$(row).find("#description").html(obj.description);
	$(row).find("#execute-link").data("id", obj.id).on(
			"click",
			function(e) {
				$.ajax({
					url : "actions/sqljobs/execute/" + $(this).data("id"),
					method : "GET",
					beforeSend : function(xhr) {
						$("#alert-text").remove();
						$("body").prepend(
								alertInfo("Executing "
										+ $(row).find("#name").text()));
					},
					success : function(data) {
						$(row).find("#lastExecutionDate").text(
								moment.utc(data.date).fromNow());
						$("#alert-text").remove();
						$("body").prepend(
								alertSuccess($(row).find("#name").text()
										+ ":&nbsp;" + data.message
										+ "&nbsp;rows queried."));
					},
					error : function(data) {
						$("#alert-text").remove();
						$("body")
								.prepend(
										alertDanger($(row).find("#name").text()
												+ ":&nbsp;"
												+ data.responseJSON.message));
					}
				});
			});
	$(row).find("#count-link").data("id", obj.id).on(
			"click",
			function(e) {
				$.ajax({
					url : "actions/sqljobs/count/" + $(this).data("id"),
					method : "GET",
					beforeSend : function(xhr) {
						$("#alert-text").remove();
						$("body").prepend(
								alertInfo("Counting "
										+ $(row).find("#name").text()));
					},
					success : function(data) {
						$(row).find("#lastExecutionDate").text(
								moment.utc(data.date).fromNow());
						$("#alert-text").remove();
						$("body").prepend(
								alertSuccess($(row).find("#name").text()
										+ ":&nbsp;" + data.message
										+ "&nbsp;rows in result."));
					},
					error : function(data) {
						$("#alert-text").remove();
						$("body")
								.prepend(
										alertDanger($(row).find("#name").text()
												+ ":&nbsp;"
												+ data.responseJSON.message));
					}
				});
			});
}

function getEntityCallback(obj) {
	$("#id").val(obj.id);
	$("#name").val(obj.name);
	$("#dbName > option");
	$("#dbName > #" + obj.dbName).attr("selected", "selected");
	ace.edit("code").setValue(obj.code);
	$("#project").val(obj.project);
	$("#csvName").val(obj.csvName);
	$("#description").val(obj.description);
}
