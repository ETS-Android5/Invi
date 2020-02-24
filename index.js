const functions = require('firebase-functions');

const admin = require("firebase-admin");

//admin.initializeApp();
admin.initializeApp(functions.config().firebase);


/* These functions are work in progress
 * At the moment when an item is returned
 * this trigger will copy the item from storeItem
 * to receipts. This is only a test but we will use
 * the data from this doc to generate the receipt
 */


exports.onRentalDel = functions.firestore
  .document('rentals/{id}')
  .onDelete((change, context) =>{

     var data = change.data();

var docRef = admin.firestore().collection("storeItem").doc(data.iid);

return docRef.get().then(function(doc) {
	if (doc.exists) {
		console.log("Document data:", doc.data());
		var addDoc = admin.firestore()
					.collection('receipts')
					.add(doc.data())
					.then(ref => {
						console.log('Added', ref.id);
					});

			return addDoc.then(res => {	console.log('Added: ', res.id); });
		} else {
			console.log("No such document!");
		}
	}).catch(function(error) {
		console.log("Error getting document:", error);
	});


});

function getCurrentDate() {
	let current_datetime = new Date();

	let formatted_date = current_datetime.getFullYear() + "-"
	+ (current_datetime.getMonth() + 1) + "-"
	+ current_datetime.getDate() + " " + current_datetime.getHours()
	+ ":" + current_datetime.getMinutes()
	 + ":" + current_datetime.getSeconds();


	return formatted_date;
}
