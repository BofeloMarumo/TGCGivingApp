/**
 * Deploy this as a Web App (Execute as: Me, Access: Anyone with the link)
 * inside the target Google Sheet's Apps Script editor, then paste the
 * deployment URL into Settings > Google Sheet > Webhook API URL.
 *
 * Routes rows to a "Giving" tab or a "Follow Up" tab based on the "type"
 * field the app sends, so both sequences land in the same spreadsheet
 * without mixing columns.
 */
function doPost(e) {
  var data = JSON.parse(e.postData.contents);
  if (!data || data.length === 0) {
    return ContentService.createTextOutput("No rows").setMimeType(ContentService.MimeType.TEXT);
  }

  var ss = SpreadsheetApp.getActiveSpreadsheet();
  var givingSheet = ss.getSheetByName("Giving") || ss.insertSheet("Giving");
  var followUpSheet = ss.getSheetByName("Follow Up") || ss.insertSheet("Follow Up");

  for (var i = 0; i < data.length; i++) {
    var row = data[i];
    if (row.type === "followup") {
      followUpSheet.appendRow([row.enrolleeName, row.phoneNumber, row.stepDayOffset, row.message, row.sentAt]);
    } else {
      givingSheet.appendRow([row.transactionId, row.dateTime, row.fullName, row.phoneNumber, row.amount, row.purpose, row.status]);
    }
  }

  return ContentService.createTextOutput("Success").setMimeType(ContentService.MimeType.TEXT);
}
