import { DeliveryType, Product, ProductStatus } from './product.model';

export function localizedProductName(product: Product, locale: string): string {
  return locale.startsWith('fr')
    ? product.productNameFr?.trim() || product.productName
    : product.productName;
}

export function localizedProductDescription(product: Product, locale: string): string {
  return locale.startsWith('fr')
    ? product.productDescriptionFr?.trim() || product.productDescription
    : product.productDescription;
}

export function localizedProductType(type: string): string {
  switch (type) {
    case 'MEDICATION':
      return $localize`:@@products.types.medication:Médicament`;
    case 'ACCESSORY':
      return $localize`:@@products.types.accessory:Accessoire`;
    case 'FOOD':
      return $localize`:@@products.types.food:Nourriture`;
    case 'EQUIPMENT':
      return $localize`:@@products.types.equipment:Équipement`;
    default:
      return type;
  }
}

export function localizedProductStatus(status: ProductStatus): string {
  switch (status) {
    case ProductStatus.AVAILABLE:
      return $localize`:@@products.status.available:Disponible`;
    case ProductStatus.PRE_ORDER:
      return $localize`:@@products.status.preOrder:Précommande`;
    case ProductStatus.OUT_OF_STOCK:
      return $localize`:@@products.status.outOfStock:Rupture de stock`;
  }
}

export function localizedDeliveryType(type: DeliveryType): string {
  switch (type) {
    case DeliveryType.DELIVERY:
      return $localize`:@@products.delivery.delivery:Livraison`;
    case DeliveryType.PICKUP:
      return $localize`:@@products.delivery.pickup:Cueillette`;
    case DeliveryType.DELIVERY_AND_PICKUP:
      return $localize`:@@products.delivery.deliveryAndPickup:Livraison et cueillette`;
    case DeliveryType.NO_DELIVERY_OPTION:
      return $localize`:@@products.delivery.none:Aucune option de livraison`;
  }
}
