package nigeriandailies.com.ng.chatting;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;


public class GroupChatActivity extends AppCompatActivity {
    private ImageButton messageSendButton;
    private EditText userMessageInput;
    private Toolbar mToolbar;
    private ScrollView mScrollView;
    private TextView displayTextMessages;

    private FirebaseAuth mAuth;
    private DatabaseReference groupRef, groupNameRef, groupMessageKeyRef;

    private String currentGroupName, currentUserId, currentUserName, currentDate, currentTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        currentGroupName = getIntent().getExtras().get("groupName").toString();


        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        groupRef = FirebaseDatabase.getInstance().getReference().child("users");
        groupNameRef = FirebaseDatabase.getInstance().getReference().child("group").child(currentGroupName);

        initializedFields();

        getUserInfo();

        messageSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessageToDatabase();
                userMessageInput.setText("");

                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();


        groupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

               if (dataSnapshot.exists()){
                   displayMessages(dataSnapshot);
               }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()){
                    displayMessages(dataSnapshot);
                }

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



    private void initializedFields() {
        messageSendButton = findViewById(R.id.send_message_button);
        userMessageInput = findViewById(R.id.input_group_message);
        mToolbar = findViewById(R.id.goup_chat_toolbar);
        mScrollView = findViewById(R.id.my_scroll_view1);
        displayTextMessages = findViewById(R.id.group_chat_text_display);
        mToolbar.setTitle(currentGroupName);
    }

    private void getUserInfo() {
        groupRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    currentUserName = dataSnapshot.child("name").getValue().toString();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    private void sendMessageToDatabase() {
      String message = userMessageInput.getText().toString();
      String messageKey = groupNameRef.push().getKey();
      if (TextUtils.isEmpty(message)){
          Toast.makeText(this, "Please write message...", Toast.LENGTH_SHORT).show();
      }
      else {
          Calendar calForDate = Calendar.getInstance();
          SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd, yyyy");
          currentDate = currentDateFormat.format(calForDate.getTime());

          Calendar calForTime = Calendar.getInstance();
          SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
          currentTime = currentTimeFormat.format(calForTime.getTime());

          HashMap<String, Object> groupMessageKey = new HashMap<>();
          groupNameRef.updateChildren(groupMessageKey);

          groupMessageKeyRef = groupNameRef.child(messageKey);

          HashMap<String, Object> messageInfoMap = new HashMap<>();
          messageInfoMap.put("name", currentUserName);
          messageInfoMap.put("message", message);
          messageInfoMap.put("date", currentDate);
          messageInfoMap.put("time", currentTime);
          groupMessageKeyRef.updateChildren(messageInfoMap);

      }
    }
    private void displayMessages(DataSnapshot dataSnapshot) {
//        Iterator iterator = dataSnapshot.getChildren().iterator();
        Iterator iterator = dataSnapshot.getChildren().iterator();
        while (iterator.hasNext()){


            String chatDate = (String) ((DataSnapshot) iterator.next()).getValue();
            String chatMessage = (String) ((DataSnapshot) iterator.next()).getValue();
            String chatName = (String) ((DataSnapshot) iterator.next()).getValue();
            String chatTime = (String) ((DataSnapshot) iterator.next()).getValue();

//            displayTextMessages.append(chatName + " :\n" + chatMessage + " :\n" + chatDate + " " + chatTime + " \n\n\n");

            displayTextMessages.append(chatName + " :\n" + chatMessage + " :\n" + chatDate + "   " + chatTime + "\n\n\n");

            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);



        }

    }
}
