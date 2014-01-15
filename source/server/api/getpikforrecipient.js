var azure = require('azure');
var qs = require('querystring');

exports.post = function(request, response) {
    var pik = request.body;
    
    var messagesTable = request.service.tables.getTable('Messages');
    messagesTable.where(function(pik) {
        return this.id == pik.id && this.toUserId == pik.toUserId;
    },pik).read({ 
		success: function(results) {
            if (results.length === 0) {
				response.send(200, { Status : "FAIL", Error : "Sorry!  Couldn't find friend request"});
                return;
			} else {
                var actualPik = results[0];
                if (actualPik.pikFileId != pik.pikFileId) {
                    response.send(200, { Status : "FAIL", Error : "Sorry!  Invalid pik information requested."});
                    return;
                } else {
                    //Get PikFile out
                    var pikFileTable = request.service.tables.getTable('PikFile');
                    pikFileTable.where({ id : actualPik.pikFileId }).read({
                        success: function(pikFiles) {
                            if (pikFiles.length === 0) {
                                response.send(200, { Status : "FAIL", Error : "Sorry!  Unable to find pik file."});
                                return;     
                            } else {
                                //Get SAS
                                var pikFile = pikFiles[0];
                                var accountName = process.env.STORAGE_ACCOUNT_NAME;
                                var accountKey = process.env.STORAGE_ACCOUNT_KEY;
                                var host = accountName + '.blob.core.windows.net';
                                var blobService = azure.createBlobService(accountName, accountKey, host);
                                var containerName = pikFile.creator.replace(":", "").toLowerCase();
                                
                                var sharedAccessPolicy = { 
                                    AccessPolicy: {
                                        Permissions: 'r', //write permissions
                                        Expiry: minutesFromNow(5) 
                                    }
                                };
                                
                                var sasUrl = blobService.generateSharedAccessSignature(containerName,
                                        pikFile.fileName, sharedAccessPolicy);                    
                                var sasQueryString = { 'sasUrl' : sasUrl.baseUrl + sasUrl.path + '?' + qs.stringify(sasUrl.queryString) };                    
                                console.log(sasQueryString);                                
                                var blobPath = sasQueryString.sasUrl;
                                
                                //Update Pik as seen! add new SeenOn date stamp to pik
                                actualPik.userHasSeen = true;
                                actualPik.seenOn = new Date();
                                actualPik.updateDate = new Date();
                                messagesTable.update(actualPik, {
                                    success: function() {
                                        console.log('success');
                                        response.send(200, { Status : "Success", PikUrl: blobPath });
                                        
                                        //Check to see if all viewers have viewed pik, if so, update original sent to be viewed
                                        //and mark pik file for deletion
                                        //actualPik.pikFileId;
                                        var sql = "SELECT id FROM Messages WHERE pikFileId = ? AND type = 'Pik' AND userHasSeen = 0";
                                        var mssql = request.service.mssql;
                                        mssql.queryRaw(sql, [actualPik.pikFileId], {
                                        	success: function(results) {
                                                if (results.rows.length == 0) {                                                    
                                                    //all piks have been seen, update original
                                                    sql = "UPDATE Messages SET allUsersHaveSeen = 1 WHERE pikFileId = ? AND type = 'SENT';UPDATE PikFile SET readyForDeletion = 1 WHERE id = ?";
                                                    mssql.queryRaw(sql, [actualPik.pikFileId, actualPik.pikFileId], {
                                                        success: function(results) { /* do nothin */ },
                                                        error: function(error) {
                                                            console.error("Couldn't update sent pik to indicate all seen: ", error);            
                                                        }
                                                    });
                                                } else {
                                                    //All piks have not been seen, do nothing 
                                                }
                                            }, error: function(error) {
                                                console.error("Couldn't check for messages not having seen pik: ", error);            
                                            }
                                        });                                                                            
                                        return;
                                    }, error: function(error) {
                                        response.send(200, { Status : "FAIL", Error : "Sorry!  There was an issue updating the pik as seen:"+ error});
                                        return; 
                                    }
                                });                                                                
                            }
                        }, error: function(error) {
                            response.send(200, { Status : "FAIL", Error : "Sorry!  There was an issue getting the error: "+ error});
                            return; 
                        }
                    });
                }
            }
        }
    });
};

function minutesFromNow(minutes) {
    var date = new Date()
  date.setMinutes(date.getMinutes() + minutes);
  return date;
}
