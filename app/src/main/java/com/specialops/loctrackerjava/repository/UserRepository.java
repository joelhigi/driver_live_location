package com.specialops.loctrackerjava.repository;

import android.content.Context;

import androidx.annotation.Nullable;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.specialops.loctrackerjava.models.Role;
import com.specialops.loctrackerjava.models.User;

public class UserRepository {
    private static volatile  UserRepository instance;
    private static final String COLLECTION_NAME="users";
    private static final String USERNAME_FIELD="username";
    private static final String ROLE_FIELD="isMentor";

    public UserRepository(){}

    public static UserRepository getInstance(){
        synchronized (UserRepository.class){
            UserRepository result = instance;
            if(result != null){
                return  result;
            }
            if(instance == null){
                instance = new UserRepository();
            }
            return instance;
        }
    }

    @Nullable
    public FirebaseUser getCurrentUser(){
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    public Task<Void> signOut(Context context){
        return AuthUI.getInstance().signOut(context);
    }

    public Task<Void> deleteUser(Context context){
        return AuthUI.getInstance().delete(context);
    }


    // firestore functions

    // get document reference
    private CollectionReference getUsersCollection(){
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME);
    }

    // create user in Firestore
    public void createUser(){
        FirebaseUser user = getCurrentUser();
        if(user !=null){
            String urlPicture = (user.getPhotoUrl() !=null) ? user.getPhotoUrl().toString() : null;
            String username = user.getDisplayName();
            String uid = user.getUid();

            User newUser = new User(uid,username, Role.DRIVER.toString(),urlPicture);

            Task<DocumentSnapshot> userData = getUserData();

            userData.addOnSuccessListener(documentSnapshot -> {
                if(documentSnapshot.contains(ROLE_FIELD)){
                    newUser.setRole("driver");
                }
                this.getUsersCollection().document(uid).set(newUser);
            });
        }
    }

    // Get Current User ID if we have current user
    public String getCurrentUserUid(){
        FirebaseUser user =  getCurrentUser();
        return (user !=null) ? user.getUid(): null;
    }

    //Get user Data from firestore
    public Task<DocumentSnapshot> getUserData(){
        String uid = this.getCurrentUserUid();
        if(uid !=null){
            return  this.getUsersCollection().document(uid).get();
        }
        return null;
    }

    // Update User username
    public Task<Void> updateUserName(String username){
        String uid = this.getCurrentUserUid();

        if(uid !=null){
            return this.getUsersCollection().document(uid).update(USERNAME_FIELD,username);
        }
        return null;
    }

    // Update User role
    public void updateRole(String role) {
        String uid = this.getCurrentUserUid();
        if(uid != null){
            this.getUsersCollection().document(uid).update(ROLE_FIELD, role);
        }
    }

    // Delete the User from Firestore
    public void deleteUserFromFirestore() {
        String uid = this.getCurrentUserUid();
        if(uid != null){
            this.getUsersCollection().document(uid).delete();
        }
    }
}
