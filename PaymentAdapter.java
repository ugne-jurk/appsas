package com.example.skaiciuotuvas;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class PaymentAdapter extends RecyclerView.Adapter<PaymentAdapter.ViewHolder> {
    private List<Payment> payments;

    public PaymentAdapter(List<Payment> payments) {
        this.payments = payments;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_payment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Payment p = payments.get(position);

        holder.month.setText("Mėnuo: " + p.month);
        holder.payment.setText(String.format("Įmoka: %.2f €", p.monthlyPayment));
        holder.principal.setText(String.format("Pagrindas: %.2f €", p.principal));
        holder.interest.setText(String.format("Palūkanos: %.2f €", p.interest));
        holder.balance.setText(String.format("Likutis: %.2f €", Math.max(0, p.balance)));

        // Atidėjimas
        if (p.isDeferred && p.deferredInterest > 0) {
            holder.deferredInfo.setVisibility(View.VISIBLE);
            holder.deferredInfo.setText(String.format("Atidėtos palūkanos: %.2f €", p.deferredInterest));
        } else {
            holder.deferredInfo.setVisibility(View.GONE);
        }

        // Show payment date if available
        if (p.paymentDate != null) {
            holder.paymentDate.setVisibility(View.VISIBLE);
            holder.paymentDate.setText("Data: " + p.getPaymentDateString());
        } else {
            holder.paymentDate.setVisibility(View.GONE);
        }


        if (p.isDeferred) {
            holder.itemView.setBackgroundColor(0xFFFFE0B2);
        } else {
            holder.itemView.setBackgroundColor(0xFFFFFFFF);
        }
    }

    @Override
    public int getItemCount() {
        return payments.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView month, payment, principal, interest, balance, deferredInfo, paymentDate;

        public ViewHolder(View itemView) {
            super(itemView);
            month = itemView.findViewById(R.id.month);
            payment = itemView.findViewById(R.id.payment);
            principal = itemView.findViewById(R.id.principal);
            interest = itemView.findViewById(R.id.interest);
            balance = itemView.findViewById(R.id.balance);
            deferredInfo = itemView.findViewById(R.id.deferredInfo);
            paymentDate = itemView.findViewById(R.id.paymentDate);
        }
    }
}