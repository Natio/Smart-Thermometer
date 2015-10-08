
function createHourRecord(year,month,day,h, callback){
  var Hour = Parse.Object.extend("Hour");
  var hour2 = new Hour();
  hour2.set("year", year);
  hour2.set("month", month);
  hour2.set("day", day);
  hour2.set("hour", h);
  hour2.set("inside", 0.0);
  hour2.set("outside", 0.0);
  hour2.set("count", 0);
  hour2.save(null, {
    success: function(newObj) {
      // Execute any logic that should take place after the object is saved.
      callback(newObj, null);
    },
    error: function(newObj, error) {
      // Execute any logic that should take place if the save fails.
      // error is a Parse.Error with an error code and message.
      callback(null, error);
    }
  });

}

function findOrCreateHourRecord(year, month, day, h, callback){
  var Hour = Parse.Object.extend("Hour");
  var hour = new Hour();
  var query = new Parse.Query(Hour);
  query.equalTo("day", day);
  query.equalTo("month", month);
  query.equalTo("year", year);
  query.equalTo("hour", h);
  query.limit(100);
  query.find({
    success: function(results) {
      if(results.length > 0){
        callback(results[0], null);
        return;
      }
      createHourRecord(year,month,day,h, callback);
    },
    error: function(error) {
      callback(null, error);
    }
  });
}

function isParseObjectNew(object){
  // workaround to fix a bug that causes ParseObject.existed() to always return false
  var seconds = (new Date().getTime() - object.get("createdAt").getTime())/1000;
  return seconds < 10;
}

Parse.Cloud.afterSave("Temperatures", function(request, response) {
  var t = request.object;
  var creation_time = t.get("createdAt");
  if(!isParseObjectNew(t)){
    return;
  }
  var inside_temp = t.get("inside");
  var outside_temp = t.get("outside");
  var time = new Date();
  var year = time.getFullYear();
  var month = time.getMonth();
  var day = time.getDate();
  var hour = time.getHours();

  findOrCreateHourRecord(year, month, day, hour, function(hourObj, error){
    if(error != null){
      console.error("Error findingOrCreating " + error.message);
      return;
    }

    var old_in_avg = hourObj.get("inside");
    var old_out_avg = hourObj.get("outside");
    var count = hourObj.get("count");

    hourObj.set("count", count + 1);
    hourObj.set("inside", (old_in_avg * count + inside_temp)/(count + 1));
    hourObj.set("outside", (old_out_avg * count + outside_temp)/(count + 1));
    hourObj.save(null, {
      success: function(newObj) {
        // Execute any logic that should take place after the object is saved.
        console.log("success");
      },
      error: function(newObj, error) {
        // Execute any logic that should take place if the save fails.
        // error is a Parse.Error with an error code and message.
        console.error('Failed to create new object, with error code: ' + error.message);
      }
    });
  }
);

});
