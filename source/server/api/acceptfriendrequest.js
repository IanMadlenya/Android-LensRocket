exports.post = function(request, response) {
    console.log(request.body);
    console.log('request from: ', request.body.fromUserId);
     response.send(200, { Status : 'SUCCESS'});
    
    //Scenario:
    //Friend A has requested to be friends with Friend B
    //Friend B has accepted the request via the PikList
    
    //Update Friends record for record from A to B
    var friendsTable = request.service.tables.getTable('Friends');
    var pik = request.body;
    
    friendsTable.where(function(pik) {
        return this.fromUserId == pik.fromUserId && this.toUserId == pik.toUserId;
    },pik).read({ 
		success: function(results) {
            if (results.length === 0) {
				response.send(200, { Status : "FAIL", Error : "Sorry!  Couldn't find friend request"});
                return;
			} else {
                var friendRequest = results[0];
                friendRequest.status = 'Connected';
                friendRequest.updateDate = new Date();
                friendsTable.update(friendRequest);
                //Add Friend record for B to A
                var newFriendRequest = {
                    fromUserId: friendRequest.toUserId,
                    toUserId:   friendRequest.fromUserId,
                    status:     'Connected',
                    createDate:  new Date(),
                    updateDate:  new Date(),
                    requestedBy: friendRequest.toUserId,
                    toUsername:  pik.fromUsername                     
                };
                friendsTable.insert(newFriendRequest);
                //Update Piks list to note friend request has been seen
                var sql = "UPDATE Messages SET userHasSeen = 1 WHERE id = ? and toUserId = ?";
                var mssql = request.service.mssql;
                mssql.queryRaw(sql, [pik.id, request.user.userId], {
                	success: function(results) {
                        response.send(200, { Status : "Success", UpdatedId: pik.id });
                    }, error: function(error) {
                        console.error("Couldn't update message: ", error);
                        response.send(200, { Status : "FAIL", Error : "Sorry!  Couldn't update Pik"});            
                    }
                });
            }
        }, error: function(error) {
            console.error("Couldn't connect friends: ", error);
            response.send(200, { Status : "FAIL", Error : "Sorry!  Couldn't connect friends"});
        }
    });    
};
