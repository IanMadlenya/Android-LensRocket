function insert(item, user, request) {
    item.fromUserId = user.userId;
    item.toUserId = user.userId;
    item.pikFileId = '';
    request.execute();

}