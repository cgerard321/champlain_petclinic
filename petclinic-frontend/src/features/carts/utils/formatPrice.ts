export const formatPrice = (price: number): string =>
  `$${price.toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',')}`;
