import { http } from './axios';

const METHOD_MAP = { credit_card: 'CREDIT_CARD', debit_card: 'DEBIT_CARD' };

export async function processPayment({ orderId, paymentMethod }) {
  const { data: payment } = await http.post('/payments', {
    orderId,
    paymentMethod: METHOD_MAP[paymentMethod] || paymentMethod,
  });
  return {
    status: payment.paymentStatus === 'COMPLETED' ? 'paid' : 'failed',
    transaction_id: payment.transactionId,
  };
}
