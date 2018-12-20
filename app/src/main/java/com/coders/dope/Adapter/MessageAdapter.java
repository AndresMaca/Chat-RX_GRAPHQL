package com.coders.dope.Adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.coders.dope.activities.R;
import com.coders.dope.database.entity.ChatMessage;

import java.util.ArrayList;
//TODO: adapt https://github.com/codename95/Android-Pagination-with-RecyclerView/blob/5f533586e2f7f0b764eef18b82492cd2ed90a4b5/app/src/main/java/com/suleiman/pagination/PaginationAdapter.java
//to the chat

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = MessageAdapter.class.getSimpleName();
    public static final int AVA_RESPONSE = 0;
    public static final int USER_RESPONSE = 1;
    private ArrayList<ChatMessage> data;


    class AvaVH extends RecyclerView.ViewHolder {
        private TextView messageText;
        private TextView date;
        private ImageView picture;

        public AvaVH(View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.date_server);
            messageText = itemView.findViewById(R.id.message_server);

        }
    }

    class UserVH extends RecyclerView.ViewHolder {
        private TextView messageText;
        private TextView date;
        private ImageView picture;

        public UserVH(View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.date_user);
            messageText = itemView.findViewById(R.id.message_user);
        }
    }


    // Provide a suitable constructor (depends on the kind of dataset)

    public MessageAdapter(ArrayList<ChatMessage> data) {
        this.data = data;

    }
    // Create new views (invoked by the layout manager)


    // Replace the contents of a view (invoked by the layout manager)


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case AVA_RESPONSE:
                View viewResponse = inflater.inflate(R.layout.message_item_server, parent, false);
                Log.d(TAG,"AVA response");
                return new AvaVH(viewResponse);

            case USER_RESPONSE:
                View userResponse = inflater.inflate(R.layout.message_item_user, parent, false);
                // userResponse = new
                Log.d(TAG,"User response");

                return new UserVH(userResponse);
            default:
                View defResponse = inflater.inflate(R.layout.message_item_server, parent, false);
                // userResponse = new
                Log.d(TAG,"User response");

                return new UserVH(defResponse);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatMessage message = data.get(position);
        //Log.d(TAG, "item type: " + holder.getItemViewType());
        //Log.d(TAG, "Date " + position + " is: " + message.getDate());
        switch (holder.getItemViewType()) {
            case AVA_RESPONSE:


                final AvaVH avaVH = (AvaVH) holder;
                avaVH.date.setText(message.getDate());
                avaVH.messageText.setText(message.getMessage());
                //avaVH.picture.setImageBitmap();
                break;
            case USER_RESPONSE:
                final UserVH userVH = (UserVH) holder;
                userVH.date.setText(message.getDate());
                userVH.messageText.setText(message.getMessage());
                break;
        }
    }


    // Return the size of your dataset (invoked by the layout manager)

    @Override
    public int getItemViewType(int position) {
       // Log.d(TAG, "ItemView Type: " + data.get(position).getUser());
        return (data.get(position).getUser()) ? USER_RESPONSE : AVA_RESPONSE;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }



}
