package nigeriandailies.com.ng.chatting.adapter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import nigeriandailies.com.ng.chatting.ImageViewerActivity;
import nigeriandailies.com.ng.chatting.MainActivity;
import nigeriandailies.com.ng.chatting.R;
import nigeriandailies.com.ng.chatting.utils.Messages;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages> userMessageList;
    private FirebaseAuth mAth;
    private DatabaseReference usersRef;

    public MessageAdapter(List<Messages> userMessageList){
        this.userMessageList = userMessageList;
    }


    public class MessageViewHolder extends RecyclerView.ViewHolder{

        public CircleImageView userImage;
        public TextView senderMessage, receiverMessage;
        public ImageView messageSenderPicture, messageReceiverPicture;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            userImage = itemView.findViewById(R.id.message_profile_image);
            senderMessage = itemView.findViewById(R.id.sender_message_text);
            receiverMessage = itemView.findViewById(R.id.receiver_message_text);
            messageSenderPicture = itemView.findViewById(R.id.message_sender_image_view);
            messageReceiverPicture = itemView.findViewById(R.id.message_receiver_image_view);


        }

    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_message_layout, parent, false);
      mAth = FirebaseAuth.getInstance();

      return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, final int position) {
        String messageSenderID = mAth.getCurrentUser().getUid();
        Messages messages = userMessageList.get(position);

        String fromUserID = messages.getFrom();
        String fromMessageType = messages.getType();

        usersRef = FirebaseDatabase.getInstance().getReference().child("users").child(fromUserID);
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild("image")){
                    String receiverProfileImage = dataSnapshot.child("image").getValue().toString();

                    Picasso.get().load(receiverProfileImage).placeholder(R.drawable.profile).into(holder.userImage);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        holder.receiverMessage.setVisibility(View.GONE);
        holder.userImage.setVisibility(View.GONE);
        holder.senderMessage.setVisibility(View.GONE);
        holder.messageSenderPicture.setVisibility(View.GONE);
        holder.messageReceiverPicture.setVisibility(View.GONE);


        if (fromMessageType.equals("text")){


            if (fromUserID.equals(messageSenderID)){
                holder.senderMessage.setVisibility(View.VISIBLE);

                holder.senderMessage.setBackgroundResource(R.drawable.sender_messages_layout);
                holder.senderMessage.setTextColor(Color.BLACK);
                holder.senderMessage.setText(messages.getMessage() + "\n\n" + messages.getTime() + " - " + messages.getDate());
            }else {

                holder.userImage.setVisibility(View.VISIBLE);
                holder.receiverMessage.setVisibility(View.VISIBLE);

                holder.receiverMessage.setBackgroundResource(R.drawable.receiver_message_layout);
                holder.receiverMessage.setTextColor(Color.BLACK);
                holder.receiverMessage.setText(messages.getMessage() + "\n\n" + messages.getTime() + " - " + messages.getDate());

            }

        } else if (fromMessageType.equals("image")){
            if (fromUserID.equals(messageSenderID)){

                holder.messageSenderPicture.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).into(holder.messageSenderPicture);
            }else {

                holder.userImage.setVisibility(View.VISIBLE);
                holder.messageReceiverPicture.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).into(holder.messageReceiverPicture);
            }

        }else if (fromMessageType.equals("pdf") || fromMessageType.equals("docx")){
            if (fromUserID.equals(messageSenderID)){
                holder.messageSenderPicture.setVisibility(View.VISIBLE);
                holder.messageSenderPicture.setBackgroundResource(R.drawable.file);

            }
            else {

                holder.userImage.setVisibility(View.VISIBLE);
                holder.messageReceiverPicture.setVisibility(View.VISIBLE);
                holder.messageReceiverPicture.setBackgroundResource(R.drawable.file);

//                holder.itemView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessageList.get(position).getMessage()));
//                        holder.itemView.getContext().startActivity(intent);
//                    }
//                });

            }
        }


        if (fromUserID.equals(messageSenderID)){
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (userMessageList.get(position).getType().equals("pdf") || userMessageList.get(position).getType().equals("docx")){
                        CharSequence options[] = new CharSequence[]{

                              "Delete for me",
                              "Download and view this document",
                                "Cancel",
                                "Delete for everyone"
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                if (i==0){

                                    deleteSentMessage(position, holder);

                                    Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }
                                 else if (i==1){
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessageList.get(position).getMessage()));
                                    holder.itemView.getContext().startActivity(intent);

                                }
//                                else if (position ==2){
//
//                                }
                                else if (i ==3){

                                    deleteMessagesForEveryOne(position,holder);

                                    Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    }

                        else if (userMessageList.get(position).getType().equals("image")){
                            CharSequence options[] = new CharSequence[]{

                                    "Delete for me",
                                    "View this Image",
                                    "Cancel",
                                    "Delete for everyone"
                            };

                            AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                            builder.setTitle("Delete Message?");
                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    if (i==0){

                                        deleteSentMessage(position,holder);

                                        Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                        holder.itemView.getContext().startActivity(intent);
                                    }
                                    else if (i ==1){

                                        Intent intent = new Intent(holder.itemView.getContext(), ImageViewerActivity.class);
                                        intent.putExtra("url", userMessageList.get(position).getMessage());
                                        holder.itemView.getContext().startActivity(intent);

                                    }

                                    else if (i ==3){

                                        deleteMessagesForEveryOne(position,holder);

                                        Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                        holder.itemView.getContext().startActivity(intent);
                                    }
                                }
                            });
                            builder.show();
                        }


                    else if (userMessageList.get(position).getType().equals("text")){
                        CharSequence options[] = new CharSequence[]{

                                "Delete for me",
//                                "Download and view this document",
                                "Cancel",
                                "Delete for everyone"
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                if (i ==0){
                                    deleteSentMessage(position,holder);

                                    Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);

                                }
                                else if (i ==2){

                                    deleteMessagesForEveryOne(position,holder);

                                    Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }
//                                else if (position ==2){
//
//                                }
                            }
                        });
                        builder.show();
                    }
//                    || userMessageList.get(position).getType().equals("text") || userMessageList.get(position).getType().equals("docx")){
//

                }
            });
        }else {

            //Receivers side

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (userMessageList.get(position).getType().equals("pdf") || userMessageList.get(position).getType().equals("docx")){
                        CharSequence options[] = new CharSequence[]{

                                "Delete for me",
                                "Download and view this document",
                                "Cancel",
//                                "Delete for everyone"
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                if (i ==0){

                                    deleteReceiveMessage(position, holder);

                                    Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }
                                else if (i ==1){
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessageList.get(position).getMessage()));
                                    holder.itemView.getContext().startActivity(intent);

                                }
//                                else if (position ==2){
//                                }

                            }
                        });
                        builder.show();
                    }

                    else if (userMessageList.get(position).getType().equals("image")){
                        CharSequence options[] = new CharSequence[]{

                                "Delete for me",
                                "View this Image",
                                "Cancel"
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                if (i ==0){

                                    deleteReceiveMessage(position, holder);
                                    Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);

                                }
                                else if (i ==1){

                                    Intent intent = new Intent(holder.itemView.getContext(), ImageViewerActivity.class);
                                    intent.putExtra("url", userMessageList.get(position).getMessage());
                                    holder.itemView.getContext().startActivity(intent);
                                }

                            }
                        });
                        builder.show();
                    }


                    else if (userMessageList.get(position).getType().equals("text")){
                        CharSequence options[] = new CharSequence[]{

                                "Delete for me",
//                                "Download and view this document",
                                "Cancel"
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                if (i ==0){

                                    deleteReceiveMessage(position, holder);

                                    Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }
//                                else if (position ==2){
//
//                                }
//                                else if (position ==2){
//
//                                }
                            }
                        });
                        builder.show();
                    }
//                    || userMessageList.get(position).getType().equals("text") || userMessageList.get(position).getType().equals("docx")){
//

                }
            });

        }
    }

    @Override
    public int getItemCount() {
        return userMessageList.size();
    }



    private void deleteSentMessage(final int position, final MessageViewHolder holder){

        final DatabaseReference rootRefe = FirebaseDatabase.getInstance().getReference();
        rootRefe.child("messages")
                .child(userMessageList.get(position).getTo())
                .child(userMessageList.get(position).getFrom())
                .child(userMessageList.get(position).getMessageID())
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            rootRefe.child("messages")
                                    .child(userMessageList.get(position).getFrom())
                                    .child(userMessageList.get(position).getTo())
                                    .child(userMessageList.get(position).getMessageID())
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        Toast.makeText(holder.itemView.getContext(), "Deleted", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        }else {
                            Toast.makeText(holder.itemView.getContext(), "Error occurred", Toast.LENGTH_SHORT).show();

                        }

                    }
                });

    }

    private void deleteReceiveMessage(final int position, final MessageViewHolder holder){

        DatabaseReference rootRefe = FirebaseDatabase.getInstance().getReference();
        rootRefe.child("messages").child(userMessageList.get(position).getTo())
                .child(userMessageList.get(position).getFrom())
                .child(userMessageList.get(position).getMessageID())
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(holder.itemView.getContext(), "Deleted", Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(holder.itemView.getContext(), "Error occurred", Toast.LENGTH_SHORT).show();

                        }

                    }
                });

    }
    private void deleteMessagesForEveryOne(final int position, final MessageViewHolder holder){

        DatabaseReference rootRefe = FirebaseDatabase.getInstance().getReference();
        rootRefe.child("messages").child(userMessageList.get(position).getFrom())
                .child(userMessageList.get(position).getTo())
                .child(userMessageList.get(position).getMessageID())
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(holder.itemView.getContext(), "Deleted", Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(holder.itemView.getContext(), "Error occurred", Toast.LENGTH_SHORT).show();

                        }

                    }
                });

    }
}
