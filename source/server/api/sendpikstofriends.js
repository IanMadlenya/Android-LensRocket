exports.post = function(request, response) {

    var postValues = request.body;
    if (postValues.members != null)
      postValues = postValues.members;
    var recipients = JSON.parse(postValues.recipients);
    var azure = require('azure');
    var notificationHubService = azure.createNotificationHubService('<YOUR-NOTIFICATION-HUB-NAME>', '<YOUR-NOTIFICATION-FULL-ACCESS-SIGNATURE>');    
    var messagesTable = request.service.tables.getTable('Messages');
    var savedMessagesCount = 0;
    for (var i = 0; i < recipients.length; i++) {
        var newMessage = { fromUserId : request.user.userId,
                          toUserId:    recipients[i],
                          type:        'Pik',
                          createDate:  new Date(),
                          updateDate:  new Date(),
                          fromUsername: postValues.fromUsername,
                          userHasSeen: false,
                          delivered:   true,
                          isPicture: postValues.isPicture,
                          isVideo: postValues.isVideo,
                          timeToLive: postValues.timeToLive,
                          pikFileId:  postValues.pikFileId
                        };
         messagesTable.insert(newMessage, {
             success: function() {
                 //Don't push to to the user sending the message
                 if (newMessage.fromUserId !== newMessage.toUserId) {
                     var payload = '{ "message" : "Friend request received", "collapse_key" : "FRIENDREQUEST" }';
                     notificationHubService.send(newMessage.toUserId, payload, 
                         function(error, outcome) {
                             console.log('issue sending push');
                             console.log('error: ', error);
                             console.log('outcome: ',outcome);
                         });
                 }
                 savedMessagesCount++;
                 if (savedMessagesCount == recipients.length) {
                     //Update original sent pik
                     var sql = "UPDATE Messages SET Delivered = 1, pikFileId = ? WHERE id = ? and fromUserId = ?";
                     var mssql = request.service.mssql;
                     mssql.queryRaw(sql, [postValues.pikFileId, postValues.originalSentPikId, request.user.userId], {
                      	success: function(results) {
                             response.send(200, { Status : "Success", 
                                                  UpdatedId: postValues.originalSentPikId,
                                                  Details: "Piks sent and original updated" 
                                                 });
                         }, error: function(error) {
                             console.error("Couldn't update original message: ", error);
                             response.send(200, { Status : "FAIL", Error : "Sorry!  Couldn't update original message as sent"});            
                         }
                     });
                 }
             }, error: function(error) {
                 console.error('Error saving message sent to recipient: ', error);
                 response.send(200, { Status: "FAIL", Error: "There was an issue sending messages to recipients."});
             }
         });
        
    }     
    response.send(200, { Status : "SUCCESS"});
};
