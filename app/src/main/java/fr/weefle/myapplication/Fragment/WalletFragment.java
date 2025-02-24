package fr.weefle.myapplication.Fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;

import fr.weefle.myapplication.Adapter.WalletAdapter;
import fr.weefle.myapplication.Model.CurrentUser;
import fr.weefle.myapplication.Model.User;
import fr.weefle.myapplication.Model.Wallet;
import fr.weefle.myapplication.R;


public class WalletFragment extends Fragment {

    //TODO the arraylist wil come from the arraylist from user account
    public ArrayList<Wallet> wallets;

    private Button addWallet;
    private EditText editWallet, editWalletBalance;
    boolean check;
    ListenerRegistration registration;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_wallet, container, false);

        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference ref = FirebaseFirestore.getInstance().collection("Users").document(userID);
        registration = ref.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {


                if(documentSnapshot != null){
                    if(documentSnapshot.toObject(User.class) != null && documentSnapshot.toObject(User.class).getWallets() != null) {
                        CurrentUser.setCurrentUser(documentSnapshot.toObject(User.class));
                        wallets = CurrentUser.getCurrentUser().getWallets();
                        //if(!wallets.isEmpty()) {
                            ListView shopListView = rootView.findViewById(R.id.wallet_list_view);
                            shopListView.setAdapter(new WalletAdapter(getActivity(), wallets));
                       // }

                    }
                }

            }
        });

        if(wallets!=null) {
            ListView shopListView = rootView.findViewById(R.id.wallet_list_view);
            shopListView.setAdapter(new WalletAdapter(getActivity(), wallets));
        }else{
            wallets = new ArrayList<>();
        }

        addWallet = rootView.findViewById(R.id.add_wallet);
        editWallet = rootView.findViewById(R.id.edit_wallet);
        editWalletBalance = rootView.findViewById(R.id.edit_wallet_balance);

        addWallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!editWallet.getText().toString().isEmpty() && !editWalletBalance.getText().toString().isEmpty()) {

                    check = false;
                    String walletName = editWallet.getText().toString();
                    if (CurrentUser.getCurrentUser() == null) {
                        CurrentUser.setCurrentUser(new User(FirebaseAuth.getInstance().getCurrentUser().getUid()));
                    }
                    for (Wallet wallet : wallets) {
                        if (wallet.getName().equals(walletName.trim())) {

                            check = true;
                        }
                    }

                    if (!check) {
                        //Transaction transaction = new Transaction("test", 0.0);
                        Wallet wallet = new Wallet(walletName, Double.parseDouble(editWalletBalance.getText().toString()));
                        //wallet.addTransaction(transaction);
                        CurrentUser.getCurrentUser().addWallet(wallet);
                        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        DocumentReference ref = FirebaseFirestore.getInstance().collection("Users").document(userID);
                        ref.set(CurrentUser.getCurrentUser()).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if (task.isSuccessful()) {
                                    Toast.makeText(getContext(), "✔ Successfully added!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        Toast.makeText(getActivity(), "Already exists!", Toast.LENGTH_SHORT).show();
                    }

                }else{
                    Toast.makeText(getContext(), "❌ They are empty fields!", Toast.LENGTH_SHORT).show();
                }
            }
        });



        return rootView;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        registration.remove();

    }
}
