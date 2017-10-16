package com.furkan.profil.Fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.support.annotation.Nullable;

import com.furkan.profil.Helpers.FriendManager;
import com.github.brnunes.swipeablerecyclerview.SwipeableRecyclerViewTouchListener;
import com.furkan.profil.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;


public class NotificationFragment extends Fragment {

    View view;
    FirebaseAuth auth;
    FirebaseUser user;
    DatabaseReference mDatabase;
    private CardViewAdapter mAdapter;
    FriendManager f;
    private ArrayList<String> mItems;
    private ArrayList<String> mUsersKeyList;
    private ArrayList<String> mType;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_notifications, container, false);

        user = FirebaseAuth.getInstance().getCurrentUser();
        f=new FriendManager();
        mItems = new ArrayList<>(30);
        mType = new ArrayList<>(30);
        mUsersKeyList = new ArrayList<>(30);
        new requestTask().execute();



        OnItemTouchListener itemTouchListener = new OnItemTouchListener() {
            @Override
            public void onCardViewTap(View view, int position) {
                Toast.makeText(getActivity(), "Tapped " + mItems.get(position), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onButton1Click(View view, int position) {
                Toast.makeText(getActivity(), "Accepted Friendship request" + mItems.get(position), Toast.LENGTH_SHORT).show();
                f.addFriend(mUsersKeyList.get(position));
                mItems.remove(position);
                mAdapter.notifyDataSetChanged();

            }

            @Override
            public void onButton2Click(View view, int position) {
                Toast.makeText(getActivity(), "Clicked Button2 in " + mItems.get(position), Toast.LENGTH_SHORT).show();
                f.rejectFriendRequest(user.getUid(),mUsersKeyList.get(position));
            }
        };

        mAdapter = new CardViewAdapter(mItems, itemTouchListener, mType);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(mAdapter);


        SwipeableRecyclerViewTouchListener swipeTouchListener =
                new SwipeableRecyclerViewTouchListener(recyclerView,
                        new SwipeableRecyclerViewTouchListener.SwipeListener() {
                            @Override
                            public boolean canSwipeLeft(int position) {
                                return true;
                            }

                            @Override
                            public boolean canSwipeRight(int position) {
                                return true;
                            }

                            @Override
                            public void onDismissedBySwipeLeft(RecyclerView recyclerView, int[] reverseSortedPositions) {
                                for (int position : reverseSortedPositions) {
//                                  Toast.makeText(MainActivity.this, mItems.get(position) + " swiped left", Toast.LENGTH_SHORT).show();
                                    if(Integer.parseInt(mType.get(position))==1){
                                        f.removeNotification(mUsersKeyList.get(position));
                                    }
                                    mItems.remove(position);
                                    mUsersKeyList.remove(position);
                                    mType.remove(position);
                                    mAdapter.notifyItemRemoved(position);
                                }
                                mAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onDismissedBySwipeRight(RecyclerView recyclerView, int[] reverseSortedPositions) {
                                for (int position : reverseSortedPositions) {
//                                  Toast.makeText(MainActivity.this, mItems.get(position) + " swiped right", Toast.LENGTH_SHORT).show();
                                    if(Integer.parseInt(mType.get(position))==1){
                                        f.removeNotification(mUsersKeyList.get(position));
                                    }
                                    mItems.remove(position);
                                    mUsersKeyList.remove(position);
                                    mType.remove(position);
                                    mAdapter.notifyItemRemoved(position);
                                }
                                mAdapter.notifyDataSetChanged();
                            }
                        });

        recyclerView.addOnItemTouchListener(swipeTouchListener);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    public interface OnItemTouchListener {

        void onCardViewTap(View view, int position);

        void onButton1Click(View view, int position);


        void onButton2Click(View view, int position);
    }


    public class requestTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            f.prepareList(new FriendManager.CheckFriendRequestListener() {
                @Override
                public void onSuccess(String uID, String Name, int type) {

                    Log.i("Fragmentsss", uID+mItems.size());
                    mItems.add(Name);
                    mUsersKeyList.add(uID);
                    mType.add(String.valueOf(type));
                    mAdapter.notifyDataSetChanged();
                }
            });
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            return;
        }
    }



    public class CardViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private List<String> cards;
        private OnItemTouchListener onItemTouchListener;
        ArrayList<String> mType;
        public CardViewAdapter(List<String> cards, OnItemTouchListener onItemTouchListener, ArrayList<String> mType) {
            this.cards = cards;
            this.onItemTouchListener = onItemTouchListener;
            this.mType=mType;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

            View v = null;
            switch (viewType) {
                case 0:  v= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_view_layout, viewGroup, false);  return new ViewHolder(v);
                case 1:  v= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_view_layout2, viewGroup, false); return new ViewHolder2(v);
            }
           return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
            //viewHolder.title.setText(cards.get(position));
            switch (getItemViewType(position)) {
                case 0:
                    ViewHolder viewHolder0 = (ViewHolder)viewHolder;
                    viewHolder0.title.setText(cards.get(position)+" isimli kullanıcı arkadaşın olmak istiyor!");
                    break;

                case 1:
                    ViewHolder2 viewHolder2 = (ViewHolder2)viewHolder;
                    viewHolder2.title.setText(cards.get(position)+" ile artık arkadaşsınız!");
                    break;
            }
        }

       @Override
       public int getItemViewType(int position) {
            return Integer.parseInt(mType.get(position));
       }

       /* @Override
        public void onBindViewHolder(ViewHolder viewHolder, int i) {
            viewHolder.title.setText(cards.get(i));
        }*/
        @Override
        public int getItemCount() {
            return cards == null ? 0 : cards.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private TextView title;
            private Button button1;
            private Button button2;

            public ViewHolder(View itemView) {
                super(itemView);
                title = (TextView) itemView.findViewById(R.id.card_view_title);
                button1 = (Button) itemView.findViewById(R.id.button_accept);
                button2 = (Button) itemView.findViewById(R.id.button_reject);

                button1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onItemTouchListener.onButton1Click(v, getLayoutPosition());
                    }
                });

                button2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onItemTouchListener.onButton2Click(v, getLayoutPosition());
                    }
                });

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onItemTouchListener.onCardViewTap(v, getLayoutPosition());
                    }
                });
            }
        }

        public class ViewHolder2 extends RecyclerView.ViewHolder {
            private TextView title;

            public ViewHolder2(View itemView) {
                super(itemView);
                title = (TextView) itemView.findViewById(R.id.card_view_title);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onItemTouchListener.onCardViewTap(v, getLayoutPosition());
                    }
                });
            }
        }




    }

}





