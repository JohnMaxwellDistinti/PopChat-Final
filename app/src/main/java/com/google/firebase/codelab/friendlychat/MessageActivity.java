/**
 * Copyright Google Inc. All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.firebase.codelab.friendlychat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener{

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView;
        ImageView messageImageView;
        TextView messengerTextView;
        CircleImageView messengerImageView;

        public MessageViewHolder(View v) {
            super(v);
            messageTextView = (TextView) itemView.findViewById(R.id.messageTextView);
            messageImageView = (ImageView) itemView.findViewById(R.id.messageImageView);
            messengerTextView = (TextView) itemView.findViewById(R.id.messengerTextView);
            messengerImageView = (CircleImageView) itemView.findViewById(R.id.messengerImageView);
        }
    }

    private static final String TAG = "MainActivity";
    public static final String MESSAGES_CHILD = "messages";
    public static final String HEMMINGSON = "Hemmingson";
    public static final String HERAK = "Herak";
    public static final String PACCAR = "Paccar";
    public static final String JUNDT = "Jundt";
    public static final String THEARC = "The Arc of Spokane";
    public static final String DOOLEY = "Dooley";
    public static String currentChatroom = "messages";
    private static final int REQUEST_IMAGE = 2;
    private static final String LOADING_IMAGE_URL = "https://www.google.com/images/spin-32.gif";
    public static final String ANONYMOUS = "anonymous";
    private String userHandle;
    private String userPhotoUrl;
    private SharedPreferences mSharedPreferences;
    private GoogleApiClient mGoogleApiClient;

    private Button submitMessage;
    private RecyclerView MsgRecyclerView;
    private LinearLayoutManager layoutManager;
    private EditText messageBox;

    // Firebase instance variables
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference firebaseRef;
    private FirebaseRecyclerAdapter<PopMessage, MessageViewHolder>
            firebaseAdapterView;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    final Uri uri = data.getData();
                    Log.d(TAG, "Uri: " + uri.toString());

                    PopMessage tempMessage = new PopMessage(null, userHandle, userPhotoUrl,
                            LOADING_IMAGE_URL);
                    Log.d("haha", "onActivityResult: "+currentChatroom);
                    firebaseRef.child(currentChatroom).push()
                            .setValue(tempMessage, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError,
                                                       DatabaseReference databaseReference) {
                                    if (databaseError == null) {
                                        String key = databaseReference.getKey();
                                        StorageReference storageReference =
                                                FirebaseStorage.getInstance()
                                                        .getReference(firebaseUser.getUid())
                                                        .child(key)
                                                        .child(uri.getLastPathSegment());

                                        //putImageInStorage(storageReference, uri, key);
                                    } else {
                                        Log.w(TAG, "Unable to write message to database.",
                                                databaseError.toException());
                                    }
                                }
                            });
                }
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        // Set default username is anonymous.
        userHandle = ANONYMOUS;
        Bundle extras = getIntent().getExtras();
        if(extras.getString("title").equals("Hemmingson")){
            currentChatroom = HEMMINGSON;
        }else if(extras.getString("title").equals("Herak")){
            currentChatroom = HERAK;
        }else if(extras.getString("title").equals("Paccar")){
            currentChatroom = PACCAR;
        }else if(extras.getString("title").equals("Jundt")){
            currentChatroom = JUNDT;
        }else if(extras.getString("title").equals("The Arc of Spokane")){
            currentChatroom = THEARC;
        }else if(extras.getString("title").equals("Dooley")){
            currentChatroom = DOOLEY;
        }else{
            currentChatroom = MESSAGES_CHILD;
        }
        TextView title = findViewById(R.id.titleText);
        title.setText(currentChatroom);
        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            userHandle = firebaseUser.getDisplayName();
            if (firebaseUser.getPhotoUrl() != null) {
                userPhotoUrl = firebaseUser.getPhotoUrl().toString();
            }
        }

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this/* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();


        // Initialize ProgressBar and RecyclerView.
        MsgRecyclerView = findViewById(R.id.messageRecyclerView);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        MsgRecyclerView.setLayoutManager(layoutManager);

        // New child entries
        firebaseRef = FirebaseDatabase.getInstance().getReference();
        SnapshotParser<PopMessage> parser = new SnapshotParser<PopMessage>() {
            @Override
            public PopMessage parseSnapshot(DataSnapshot dataSnapshot) {
                PopMessage popMessage = dataSnapshot.getValue(PopMessage.class);
                if (popMessage != null) {
                    popMessage.setId(dataSnapshot.getKey());
                }
                return popMessage;
            }
        };

        DatabaseReference messagesRef = firebaseRef.child(currentChatroom);
        FirebaseRecyclerOptions<PopMessage> options =
                new FirebaseRecyclerOptions.Builder<PopMessage>()
                        .setQuery(messagesRef, parser)
                        .build();
        firebaseAdapterView = new FirebaseRecyclerAdapter<PopMessage, MessageViewHolder>(options) {
            @Override
            public MessageViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
                LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                return new MessageViewHolder(inflater.inflate(R.layout.item_message, viewGroup, false));
            }

            @Override
            protected void onBindViewHolder(final MessageViewHolder viewHolder,
                                            int position,
                                            PopMessage popMessage) {
                if (popMessage.getText() != null) {
                    viewHolder.messageTextView.setText(popMessage.getText());
                    viewHolder.messageTextView.setVisibility(TextView.VISIBLE);
                    viewHolder.messageImageView.setVisibility(ImageView.GONE);
                } else if (popMessage.getImageUrl() != null) {
                    String imageUrl = popMessage.getImageUrl();
                    if (imageUrl.startsWith("gs://")) {
                        StorageReference storageReference = FirebaseStorage.getInstance()
                                .getReferenceFromUrl(imageUrl);
                        storageReference.getDownloadUrl().addOnCompleteListener(
                                new OnCompleteListener<Uri>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Uri> task) {
                                        if (task.isSuccessful()) {
                                            String downloadUrl = task.getResult().toString();
                                            Glide.with(viewHolder.messageImageView.getContext())
                                                    .load(downloadUrl)
                                                    .into(viewHolder.messageImageView);
                                        } else {
                                            Log.w(TAG, "Getting download url was not successful.",
                                                    task.getException());
                                        }
                                    }
                                });
                    } else {
                        Glide.with(viewHolder.messageImageView.getContext())
                                .load(popMessage.getImageUrl())
                                .into(viewHolder.messageImageView);
                    }
                    viewHolder.messageImageView.setVisibility(ImageView.VISIBLE);
                    viewHolder.messageTextView.setVisibility(TextView.GONE);
                }


                viewHolder.messengerTextView.setText(popMessage.getName());
                if (popMessage.getPhotoUrl() == null) {
                    viewHolder.messengerImageView.setImageDrawable(ContextCompat.getDrawable(MessageActivity.this,
                            R.drawable.nongmailuser));
                } else {
                    Glide.with(MessageActivity.this).load(popMessage.getPhotoUrl()).into(viewHolder.messengerImageView);
                }

            }
        };


        firebaseAdapterView.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int popMessageCount = firebaseAdapterView.getItemCount();
                int lastVisiblePosition = layoutManager.findLastCompletelyVisibleItemPosition();
                if (lastVisiblePosition == -1 || (positionStart >= (popMessageCount - 1) && lastVisiblePosition == (positionStart - 1))){
                    MsgRecyclerView.scrollToPosition(positionStart);
                }
            }
        });

        MsgRecyclerView.setAdapter(firebaseAdapterView);

        messageBox = findViewById(R.id.messageEditText);
        messageBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    submitMessage.setEnabled(true);
                } else {
                    submitMessage.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        submitMessage = findViewById(R.id.sendButton);
        submitMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopMessage popMessage = new PopMessage(messageBox.getText().toString(), userHandle, userPhotoUrl, null);
                firebaseRef.child(currentChatroom).push().setValue(popMessage);
                messageBox.setText("");
            }
        });
        
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in.
        // TODO: Add code to check if user is signed in.
    }

    @Override
    public void onPause() {
        firebaseAdapterView.stopListening();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        firebaseAdapterView.startListening();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }
}