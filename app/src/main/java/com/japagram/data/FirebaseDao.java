package com.japagram.data;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

public class FirebaseDao {

    private static final String DEBUG_TAG = "JT DB Debug";
    private final DatabaseReference mFirebaseDbReference;
    private ValueEventListener mEventListenerGetUniqueObject;
    private ValueEventListener mEventListenerGetObjectByValuePair;
    private ValueEventListener mEventListenerUpdateKeyValuePair;
    private ValueEventListener mEventListenerUpdateObject;
    private ValueEventListener mEventListenerGetFullObjectsList;


    public FirebaseDao(FirebaseOperationsHandler listener) {
        this.mOnOperationPerformedHandler = listener;
        mFirebaseDbReference = FirebaseDatabase.getInstance().getReference();
    }


    //Firebase Database CRUD methods
    private String addObjectToFirebaseDb(Object object) {

        DatabaseReference firebaseDbReference = FirebaseDatabase.getInstance().getReference();

        String key = "";
        if (object instanceof Word) {
            Word word = (Word) object;

            //Setting the family's unique identifier
            word.setUniqueIdentifierFromDetails();

            //Creating the word in Firebase
            key = word.getUniqueIdentifier();
            firebaseDbReference.child("wordsList").child(key).setValue(word);
        }

        return key;
    }
    public void addObjectsToFirebaseDb(Object objectsData) {

        try {
            List<Object> objectsList = (List<Object>) objectsData;
            for (int i = 0; i < objectsList.size(); i++) {
                Object object = objectsList.get(i);
                if (object != null) {
                    if (object instanceof Word) {
                        Word word = (Word) object;
                        if (word.getIsCommon()) addObjectToFirebaseDb(object);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public void getUniqueObjectFromFirebaseDbOrCreateIt(Object object, boolean onlyOnce) {

        DatabaseReference firebaseDbReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference reference = null;

        if (object instanceof Word) {
            Word word = (Word) object;
            reference = firebaseDbReference.child("wordsList").child(word.getUniqueIdentifier());
            mEventListenerGetUniqueObject = createListenerForUniqueObject(new Word());
        }

        if (reference!=null) {
            if (onlyOnce) reference.addListenerForSingleValueEvent(mEventListenerGetUniqueObject);
            else reference.addValueEventListener(mEventListenerGetUniqueObject);
        }

    }
    public void getObjectsByKeyValuePairFromFirebaseDb(Object object, String key, String value, boolean onlyOnce) {

        DatabaseReference firebaseDbReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference reference = null;

        if (object instanceof Word) {
            reference = firebaseDbReference.child("wordsList");
            mEventListenerGetObjectByValuePair = createListenerForObjectList(new Word());
        }

        if (reference!=null) {
            Query objectWithKeyQuery = reference.orderByChild(key).equalTo(value);
            if (onlyOnce) objectWithKeyQuery.addListenerForSingleValueEvent(mEventListenerGetObjectByValuePair);
            else objectWithKeyQuery.addValueEventListener(mEventListenerGetObjectByValuePair);
        }

    }
    public void getFullObjectsListFromFirebaseDb(Object object, boolean onlyOnce) {

        DatabaseReference firebaseDbReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference reference = null;

        if (object instanceof Word) {
            reference = firebaseDbReference.child("wordsList");
            mEventListenerGetFullObjectsList = createListenerForObjectList(new Word());
        }

        if (reference!=null) {
            if (onlyOnce) reference.addListenerForSingleValueEvent(mEventListenerGetFullObjectsList);
            else reference.addValueEventListener(mEventListenerGetFullObjectsList);
        }

    }
    private void updateObjectKeyValuePairInFirebaseDb(Object object, final String key, final Object value, boolean onlyOnce) {

        final DatabaseReference firebaseDbReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference reference = null;

        if (object instanceof Word) {
            Word word = (Word) object;
            reference = firebaseDbReference.child("wordsList");
            mEventListenerUpdateKeyValuePair = createListenerForUpdatingObjectKeyValuePair(word.getUniqueIdentifier(), key, value, reference);
            reference.addValueEventListener(mEventListenerUpdateKeyValuePair);
        }

        if (reference!=null) {
            if (onlyOnce) reference.addListenerForSingleValueEvent(mEventListenerUpdateKeyValuePair);
            else reference.addValueEventListener(mEventListenerUpdateKeyValuePair);
        }
    }
    private void updateObjectOrCreateItInFirebaseDb(Object object, boolean onlyOnce, String languageCode) {

        final DatabaseReference firebaseDbReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference reference = null;

        if (object instanceof Word) {
            Word word = (Word) object;
            reference = firebaseDbReference.child("wordsList" + languageCode);
            mEventListenerUpdateObject = createListenerForUpdatingObject(word.getUniqueIdentifier(), word, reference);
        }

        if (reference!=null) {
            if (onlyOnce) reference.addListenerForSingleValueEvent(mEventListenerUpdateObject);
            else reference.addValueEventListener(mEventListenerUpdateObject);
        }
    }
    public void updateObjectsOrCreateThemInFirebaseDb(Object objectsData) {

        try {
            List<Object> objectsList = (List<Object>) objectsData;
            for (Object object : objectsList) {
                updateObjectOrCreateItInFirebaseDb(object, true, "");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public void deleteObjectFromFirebaseDb(Object object) {

        DatabaseReference firebaseDbReference = FirebaseDatabase.getInstance().getReference();

        if (object instanceof Word) {
            Word word = (Word) object;
            firebaseDbReference.child("dogsList").child(word.getUniqueIdentifier()).removeValue();
        }
    }


    //Firebase Database Helper methods (prevent code repetitions in the CRUD methods)
    private void sendObjectListToInterface(DataSnapshot dataSnapshot, Object object) {

        if (object instanceof Word) {
            List<Word> wordsList = new ArrayList<>();
            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                wordsList.add(ds.getValue(Word.class));
            }
            mOnOperationPerformedHandler.onWordsListFound(wordsList);
        }
    }
    private void sendUniqueObjectListToInterface(DataSnapshot dataSnapshot, Object object) {

        if (object instanceof Word) {
            List<Word> wordsList = new ArrayList<>();
            wordsList.add(dataSnapshot.getValue(Word.class));
            mOnOperationPerformedHandler.onWordsListFound(wordsList);
        }
    }
    private ValueEventListener createListenerForObjectList(final Object object) {
        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                sendObjectListToInterface(dataSnapshot, object);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        };
        return eventListener;
    }
    private ValueEventListener createListenerForUniqueObject(final Object object) {
        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                sendUniqueObjectListToInterface(dataSnapshot, object);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //If the object was not found, then try to update it. If that fails, then the object is truly missing so create it
                updateObjectOrCreateItInFirebaseDb(object, true, "");
            }
        };
        return eventListener;
    }
    private ValueEventListener createListenerForUpdatingObjectKeyValuePair(final String uniqueId, final String key, final Object value, final DatabaseReference reference) {
        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(uniqueId)) {
                    reference.child(uniqueId).child(key).setValue(value);
                }
                else {
                    Log.i(DEBUG_TAG,"Japanese Toolbox: Firebase error - tried to update non-existent object!");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        };
        return eventListener;
    }
    private ValueEventListener createListenerForUpdatingObject(final String uniqueId, final Object object, final DatabaseReference reference) {
        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(uniqueId)) {
                    reference.child(uniqueId).setValue(object);
                }
                else {
                    addObjectToFirebaseDb(object);
                    Log.i(DEBUG_TAG,"Japanese Toolbox: Firebase event - tried to update non-existent object, creating it instead");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                addObjectToFirebaseDb(object);
            }
        };
        return eventListener;
    }


    //Communication with other activities/fragments
    final private FirebaseOperationsHandler mOnOperationPerformedHandler;
    public interface FirebaseOperationsHandler {
        void onWordsListFound(List<Word> wordsList);
    }
    public void removeListeners() {
        if (mEventListenerGetUniqueObject!=null) mFirebaseDbReference.removeEventListener(mEventListenerGetUniqueObject);
        if (mEventListenerGetObjectByValuePair!=null) mFirebaseDbReference.removeEventListener(mEventListenerGetObjectByValuePair);
        if (mEventListenerUpdateKeyValuePair!=null) mFirebaseDbReference.removeEventListener(mEventListenerUpdateKeyValuePair);
        if (mEventListenerUpdateObject!=null) mFirebaseDbReference.removeEventListener(mEventListenerUpdateObject);
        if (mEventListenerGetFullObjectsList!=null) mFirebaseDbReference.removeEventListener(mEventListenerGetFullObjectsList);
    }
}
