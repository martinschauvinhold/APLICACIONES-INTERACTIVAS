export function normalizeAddress(a) {
  return {
    address_id: a.id,
    user_id: a.userId,
    street: a.street,
    city: a.city,
    state: a.state,
    zip_code: a.zipCode,
    reference_note: a.referenceNote,
  };
}
