package com.aluminati.inventory;

public class Constants {

    public static final int CAMERA_REQUEST = 0x222;
    public static final int SCAN_SUCCESS = 0x223;

    public static final String SCAN_FRAG = "Scan";
    public static final String PROFILE_FRAG = "Profile";
    public static final String MY_ITEMS_FRAG = "MyItems";
    public static final int SCANNER_STARTED = 0x1234;
    public static final int SCANNER_FINISHED = 0x1235;

    private Constants() {}

    public class FirestoreCollections {
        public static final String RENTALS = "rentals";
        public static final String STORES = "stores";
        public static final String STORE_ITEMS = "storeItem";
        public static final String LIVE_USER_CART = "liveUserCart/%s/items";
        public static final String USER_RENTALS = "userRentals/%s/items";
        public static final String USERS = "users";
    }
}
