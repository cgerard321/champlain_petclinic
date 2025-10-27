import { useCallback, useEffect, useState } from 'react';
import { useToast } from '@/shared/components/toast/ToastProvider';
import CartBillingForm, { BillingInfo } from './CartBillingForm';
import InvoiceComponent, {
  InvoiceFull as InvoiceFullType,
  InvoiceItem as InvoiceItemType,
} from './Invoice';
import { useNavigate, useParams } from 'react-router-dom';
import CartItem from './CartItem';
import { ProductModel } from '../models/ProductModel';
import './cart-shared.css';
import './UserCart.css';
import { NavBar } from '@/layouts/AppNavBar';
import { FaShoppingCart } from 'react-icons/fa';
import ImageContainer from '@/features/products/components/ImageContainer';
import axiosInstance from '@/shared/api/axiosInstance';
import getErrorMessage from '@/shared/api/getErrorMessage';
import { formatPrice } from '../utils/formatPrice';
import {
  applyPromo,
  clearPromo,
  type CartDetailsModel,
} from '@/shared/api/cart';
import {
  IsAdmin,
  IsInventoryManager,
  IsReceptionist,
  IsVet,
  useUser,
} from '@/context/UserContext';
import { AppRoutePaths } from '@/shared/models/path.routes';
import {
  notifyCartChanged,
  setCartCountInLS,
  setCartIdInLS,
} from '../api/cartEvent';
import { computeTaxes, formatTaxRate, roundToCents } from '../utils/taxUtils';
import { useConfirmModal } from '@/shared/hooks/useConfirmModal';
interface ProductAPIResponse {
  productId: string;
  imageId?: string;
  productName: string;
  productDescription?: string;
  productSalePrice: number;
  averageRating?: number;
  quantityInCart?: number;
  productQuantity?: number;
}

interface Invoice {
  productId: number;
  productName: string;
  productSalePrice: number;
  quantity: number;
}

/**
 * UserCart component displays the user's shopping cart, allowing them to view, update, and remove items,
 * manage billing information, and proceed to checkout. It also handles displaying invoices and notifications,
 * and interacts with the backend to fetch and update cart data.
 */
const UserCart: React.FC = () => {
  const navigate = useNavigate();
  const { cartId } = useParams<{ cartId?: string }>();
  const { confirm, ConfirmModal } = useConfirmModal();
  const { user } = useUser();
  const { showToast } = useToast();
  const customerId = user?.userId ?? null;

  const [cartItems, setCartItems] = useState<ProductModel[]>([]);
  const [wishlistItems, setWishlistItems] = useState<ProductModel[]>([]);

  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [errorMessages, setErrorMessages] = useState<Record<number, string>>(
    {}
  );

  const [checkoutMessage, setCheckoutMessage] = useState<string | null>(null);
  const [showInvoiceModal, setShowInvoiceModal] = useState<boolean>(false);
  const [lastInvoice, setLastInvoice] = useState<InvoiceFullType | null>(null);
  const [cartItemCount, setCartItemCount] = useState<number>(0);
  const [isCheckoutModalOpen, setIsCheckoutModalOpen] =
    useState<boolean>(false);
  const [showBillingForm, setShowBillingForm] = useState<boolean>(false);
  const [billingInfo, setBillingInfo] = useState<BillingInfo | null>(null);

  const [voucherCode, setVoucherCode] = useState<string>('');
  const [voucherError, setVoucherError] = useState<string | null>(null);

  const [movingAll, setMovingAll] = useState<boolean>(false);

  const [promoPercent, setPromoPercent] = useState<number | null>(null);

  const toProductModel = useCallback(
    (product: ProductAPIResponse): ProductModel => ({
      productId: String(product.productId ?? ''),
      imageId: product.imageId ?? '',
      productName: product.productName,
      productDescription: product.productDescription ?? '',
      productSalePrice: product.productSalePrice,
      averageRating: product.averageRating,
      quantity:
        product.quantityInCart && product.quantityInCart > 0
          ? product.quantityInCart
          : 1,
      productQuantity: product.productQuantity ?? 0,
    }),
    []
  );

  const emitCartMessage = useCallback(
    (serverMessage?: string | null, fallbackMessage?: string) => {
      const text = (serverMessage ?? '').trim() || fallbackMessage;
      if (!text) return;
      const tone = serverMessage?.trim() ? 'info' : 'success';
      showToast(text, tone);
    },
    [showToast]
  );

  const getStatusCode = useCallback((err: unknown): number | undefined => {
    return (err as { response?: { status?: number } })?.response?.status;
  }, []);

  const apiErrorMessage = useCallback(
    (err: unknown, defaultMessage: string): string => {
      if (getStatusCode(err) === 404) return 'Cart not found';
      return getErrorMessage(err, { defaultMessage });
    },
    [getStatusCode]
  );

  const ensureCartId = useCallback((): string | undefined => {
    if (!cartId) {
      const message = 'Invalid cart ID';
      setError(message);
      showToast(message, 'error');
      return undefined;
    }
    return cartId;
  }, [cartId, setError, showToast]);

  const syncCartState = useCallback(
    (
      cartData: CartDetailsModel | null | undefined,
      fallbackMessage?: string
    ) => {
      if (!cartData) return;

      const normalizedCartItems = Array.isArray(cartData.products)
        ? cartData.products.map(item =>
            toProductModel(item as ProductAPIResponse)
          )
        : [];

      setCartItems(normalizedCartItems);

      const cartCount = normalizedCartItems.reduce(
        (acc, item) => acc + (item.quantity ?? 0),
        0
      );
      setCartItemCount(cartCount);
      setCartCountInLS(cartCount);
      notifyCartChanged();

      if (cartData.cartId) {
        setCartIdInLS(cartData.cartId);
      }

      const normalizedWishlist = Array.isArray(cartData.wishListProducts)
        ? cartData.wishListProducts.map(item =>
            toProductModel(item as ProductAPIResponse)
          )
        : [];
      setWishlistItems(normalizedWishlist);

      setPromoPercent(
        typeof cartData.promoPercent === 'number' ? cartData.promoPercent : null
      );

      emitCartMessage(
        typeof cartData.message === 'string' ? cartData.message : undefined,
        fallbackMessage
      );
    },
    [emitCartMessage, toProductModel]
  );

  // Recent purchases state
  const [recentPurchases, setRecentPurchases] = useState<
    Array<{
      productId: string;
      productName: string;
      productSalePrice: number;
      imageId: string;
      quantity: number;
    }>
  >([]);

  // Quantity state for recent purchases
  const [recentPurchaseQuantities, setRecentPurchaseQuantities] = useState<{
    [productId: string]: number;
  }>({});

  // Handle quantity change for recent purchases
  const handleRecentPurchaseQuantityChange = (
    productId: string,
    value: number
  ): void => {
    setRecentPurchaseQuantities(prev => ({
      ...prev,
      [productId]: Math.max(1, value),
    }));
  };

  // Handle 'Purchase Again' action
  const handlePurchaseAgain = async (item: {
    productId: string;
    productName: string;
    productSalePrice: number;
    imageId: string;
    quantity: number;
  }): Promise<void> => {
    const existingCartId = ensureCartId();
    if (!existingCartId) return;

    const quantity = Math.max(1, recentPurchaseQuantities[item.productId] || 1);

    try {
      const { data } = await axiosInstance.post<CartDetailsModel>(
        `/carts/${existingCartId}/products`,
        {
          productId: item.productId,
          quantity,
        },
        { useV2: false }
      );

      syncCartState(data, `${item.productName} (x${quantity}) added to cart!`);
    } catch (err: unknown) {
      showToast(
        apiErrorMessage(err, `Failed to add ${item.productName} to cart.`),
        'error'
      );
    }
  };

  // Recommendation purchases state
  const [recommendationPurchases, setRecommendationPurchases] = useState<
    Array<{
      productId: string;
      productName: string;
      productSalePrice: number;
      imageId: string;
      quantity: number;
      averageRating?: number;
    }>
  >([]);

  // Quantity state for recommendation purchases
  const [
    recommendationPurchaseQuantities,
    setRecommendationPurchaseQuantities,
  ] = useState<{
    [productId: string]: number;
  }>({});

  // Handle quantity change for recommendation purchases
  const handleRecommendationPurchaseQuantityChange = (
    productId: string,
    value: number
  ): void => {
    setRecommendationPurchaseQuantities(prev => ({
      ...prev,
      [productId]: Math.max(1, value),
    }));
  };

  // Handle 'Purchase Recommendation' action
  const handlePurchaseRecommendation = async (item: {
    productId: string;
    productName: string;
    productSalePrice: number;
    imageId: string;
    quantity: number;
  }): Promise<void> => {
    const existingCartId = ensureCartId();
    if (!existingCartId) return;

    const quantity = Math.max(
      1,
      recommendationPurchaseQuantities[item.productId] || 1
    );

    try {
      const { data } = await axiosInstance.post<CartDetailsModel>(
        `/carts/${existingCartId}/products`,
        {
          productId: item.productId,
          quantity,
        },
        { useV2: false }
      );
      syncCartState(data, `${item.productName} (x${quantity}) added to cart!`);
    } catch (err: unknown) {
      showToast(
        apiErrorMessage(err, `Failed to add ${item.productName} to cart.`),
        'error'
      );
    }
  };

  // rôles (read-only pour staff/admin)
  const isAdmin = IsAdmin();
  const isStaff =
    isAdmin || IsInventoryManager() || IsVet() || IsReceptionist();

  // derived totals
  const subtotal = cartItems.reduce(
    (acc, item) => acc + item.productSalePrice * (item.quantity || 1),
    0
  );

  const promoDiscount =
    promoPercent != null ? (subtotal * promoPercent) / 100 : 0;
  const discountedSubtotal = Math.max(0, subtotal - promoDiscount);
  const resolvedTaxLines = computeTaxes(
    discountedSubtotal,
    billingInfo?.province
  ).map(line => ({
    ...line,
    amount: line.amount ?? roundToCents(discountedSubtotal * line.rate),
  }));
  const totalTax = roundToCents(
    resolvedTaxLines.reduce((acc, line) => acc + (line.amount ?? 0), 0)
  );

  const effectiveDiscount = promoDiscount;

  const total = roundToCents(discountedSubtotal + totalTax);

  const updateCartItemCount = useCallback(() => {
    const count = cartItems.reduce(
      (acc, item) => acc + (item.quantity || 0),
      0
    );
    setCartItemCount(count);
  }, [cartItems]);

  useEffect(() => {
    const fetchCartItems = async (): Promise<void> => {
      if (!cartId) {
        setError('Invalid cart ID');
        setLoading(false);
        return;
      }

      try {
        const { data } = await axiosInstance.get<CartDetailsModel>(
          `/carts/${cartId}`,
          {
            useV2: false,
          }
        );

        syncCartState(data);
      } catch (err: unknown) {
        console.error(err);
        setError('Failed to fetch cart items');
      } finally {
        setLoading(false);
      }
    };

    fetchCartItems();
  }, [cartId, syncCartState]);

  useEffect(() => {
    updateCartItemCount();
  }, [cartItems, updateCartItemCount]);

  // Fetch recent purchases
  useEffect(() => {
    if (!customerId) {
      setRecentPurchases([]);
      return;
    }

    const fetchRecentPurchases = async (): Promise<void> => {
      try {
        const { data } = await axiosInstance.get(
          `/customers/${customerId}/cart/recent-purchases`,
          { useV2: false }
        );
        setRecentPurchases(data || []);
      } catch (err) {
        setRecentPurchases([]);
      }
    };

    fetchRecentPurchases();
  }, [customerId]);

  // Fetch recommendation purchases
  // Reusable function to fetch recommendation purchases
  const fetchRecommendationPurchases = useCallback(async (): Promise<void> => {
    if (!customerId) {
      setRecommendationPurchases([]);
      return;
    }
    try {
      const { data } = await axiosInstance.get(
        `/customers/${customerId}/cart/recommendation-purchases`,
        { useV2: false }
      );
      setRecommendationPurchases(data || []);
    } catch (err) {
      setRecommendationPurchases([]);
    }
  }, [customerId]);

  useEffect(() => {
    fetchRecommendationPurchases();
  }, [fetchRecommendationPurchases]);

  const applyVoucherCode = async (): Promise<void> => {
    const existingCartId = ensureCartId();
    if (!existingCartId) {
      setVoucherError('Invalid cart ID');
      return;
    }
    if (!voucherCode.trim()) {
      setVoucherError('Please enter a promo code.');
      return;
    }

    try {
      const { data } = await axiosInstance.get(
        `/carts/promos/validate/${encodeURIComponent(voucherCode.trim())}`,
        { useV2: false, handleLocally: true }
      );

      const percent: number = Number(data?.discount);
      if (Number.isNaN(percent) || percent < 0 || percent > 100) {
        setVoucherError('Received invalid discount from server.');
        return;
      }

      const updated = await applyPromo(existingCartId, percent);

      setVoucherError(null);
      const appliedMessage = `Promo applied${
        updated?.promoPercent != null
          ? `: ${updated.promoPercent}%`
          : `: ${percent}%`
      }`;
      syncCartState(updated, appliedMessage);
    } catch (err) {
      console.error('Error validating promo code:', err);
      setVoucherError('Promo code invalid or expired.');
    }
  };

  const blockIfReadOnly = useCallback((): boolean => {
    if (isStaff) {
      showToast('Read-only mode: staff/admin cannot modify carts.', 'info');
      return true;
    }
    return false;
  }, [isStaff, showToast]);

  const changeItemQuantity = useCallback(
    async (
      event: React.ChangeEvent<HTMLInputElement>,
      index: number
    ): Promise<void> => {
      if (blockIfReadOnly()) return;
      const existingCartId = ensureCartId();
      if (!existingCartId) return;

      const newQuantity = Math.max(1, Number(event.target.value));
      const item = cartItems[index];
      if (newQuantity > item.productQuantity) {
        setErrorMessages(prevErrors => ({
          ...prevErrors,
          [index]: `You cannot add more than ${item.productQuantity} items. Only ${item.productQuantity} items left in stock.`,
        }));
        return;
      } else {
        setErrorMessages(prevErrors => {
          const rest = { ...prevErrors };
          delete rest[index];
          return rest;
        });
      }

      try {
        const { data } = await axiosInstance.patch<CartDetailsModel>(
          `/carts/${existingCartId}/products/${item.productId}`,
          { quantity: newQuantity },
          { useV2: false }
        );

        if (data?.message) {
          if (data.message.includes('moved to your wishlist')) {
            setErrorMessages(prevErrors => {
              const next = { ...prevErrors };
              delete next[index];
              return next;
            });
            syncCartState(data, data.message);
            return;
          }

          setErrorMessages(prevErrors => ({
            ...prevErrors,
            [index]: data.message ?? 'Failed to update quantity',
          }));
          syncCartState(data, data.message);
          return;
        }

        setErrorMessages(prevErrors => {
          const next = { ...prevErrors };
          delete next[index];
          return next;
        });
        syncCartState(data, 'Item quantity updated successfully.');
      } catch (err) {
        console.error('Error updating quantity:', err);
        setErrorMessages(prev => ({
          ...prev,
          [index]: 'Failed to update quantity',
        }));
      }
    },
    [cartItems, blockIfReadOnly, ensureCartId, syncCartState]
  );

  const onClearPromo = async (): Promise<void> => {
    const existingCartId = ensureCartId();
    if (!existingCartId) return;
    try {
      await clearPromo(existingCartId);
      setPromoPercent(null);
      showToast('Promo removed.', 'success');
    } catch (err: unknown) {
      showToast(apiErrorMessage(err, 'Failed to remove promo.'), 'error');
    }
  };

  const deleteItem = useCallback(
    async (productId: string, indexToDelete: number): Promise<void> => {
      if (blockIfReadOnly()) return;
      const existingCartId = ensureCartId();
      if (!existingCartId) return;

      const ok = await confirm({
        title: 'Remove item',
        message: 'Remove this item from your cart?',
        confirmText: 'Remove',
        cancelText: 'Cancel',
        variant: 'danger',
      });
      if (!ok) return;

      try {
        await axiosInstance.delete(
          `/carts/${existingCartId}/products/${productId}`,
          {
            useV2: false,
          }
        );
        // Expect a 204 response here; any errors are handled below.

        setCartItems(prev => {
          const next = prev.filter((_, idx) => idx !== indexToDelete);
          const nextCount = next.reduce(
            (acc, cartItem) => acc + (cartItem.quantity || 0),
            0
          );
          setCartItemCount(nextCount);
          setCartCountInLS(nextCount);
          notifyCartChanged();
          return next;
        });
        showToast('Item removed from cart.', 'success');
      } catch (error) {
        console.error('Error deleting item: ', error);
        showToast(apiErrorMessage(error, 'Failed to delete item.'), 'error');
      }
    },
    [apiErrorMessage, blockIfReadOnly, confirm, ensureCartId, showToast]
  );

  const clearCart = async (): Promise<void> => {
    if (blockIfReadOnly()) return;
    const existingCartId = ensureCartId();
    if (!existingCartId) return;

    const ok = await confirm({
      title: 'Clear cart',
      message: 'Are you sure you want to clear the cart?',
      confirmText: 'Clear cart',
      cancelText: 'Cancel',
      variant: 'danger',
    });
    if (!ok) return;

    try {
      await axiosInstance.delete(`/carts/${existingCartId}/products`, {
        useV2: false,
      });
      setCartItems([]);
      setCartItemCount(0);
      setCartCountInLS(0);
      notifyCartChanged();
      showToast('Cart has been cleared.', 'success');
    } catch (error) {
      console.error('Error clearing cart:', error);
      showToast(apiErrorMessage(error, 'Failed to clear cart.'), 'error');
    }
  };

  const addToWishlist = async (item: ProductModel): Promise<void> => {
    if (blockIfReadOnly()) return;

    const existingCartId = ensureCartId();
    if (!existingCartId) return;

    try {
      const productId = item.productId;

      const normalizedId = productId?.trim();
      const payload = {
        productIds: normalizedId ? [normalizedId] : [],
        direction: 'TO_WISHLIST',
      };

      const { data } = await axiosInstance.post<CartDetailsModel>(
        `/carts/${existingCartId}/wishlist-transfers`,
        payload,
        { useV2: false }
      );

      const fallback = `${item.productName} has been added to your wishlist!`;
      syncCartState(data, fallback);
    } catch (error: unknown) {
      console.error('Error adding to wishlist:', error);
      showToast(
        apiErrorMessage(error, 'Failed to add item to wishlist.'),
        'error'
      );
    }
  };

  const addToCartFunction = async (item: ProductModel): Promise<void> => {
    if (blockIfReadOnly()) return;

    const existingCartId = ensureCartId();
    if (!existingCartId) return;

    if (item.productQuantity <= 0) {
      showToast(
        `${item.productName} is out of stock and cannot be added to the cart.`,
        'info'
      );
      return;
    }
    try {
      const productId = item.productId;

      const normalizedId = productId?.trim();
      const payload = {
        productIds: normalizedId ? [normalizedId] : [],
        direction: 'TO_CART',
      };

      const { data } = await axiosInstance.post<CartDetailsModel>(
        `/carts/${existingCartId}/wishlist-transfers`,
        payload,
        { useV2: false }
      );

      const fallback = `${item.productName} has been added to your cart!`;
      syncCartState(data, fallback);
    } catch (error: unknown) {
      console.error('Error adding to cart:', error);
      showToast(apiErrorMessage(error, 'Failed to add item to cart.'), 'error');
    }
  };

  const removeFromWishlist = async (item: ProductModel): Promise<void> => {
    if (blockIfReadOnly()) return;

    const existingCartId = ensureCartId();
    if (!existingCartId) return;

    const ok = await confirm({
      title: 'Remove from wishlist',
      message: `Remove "${item.productName}" from wishlist?`,
      confirmText: 'Remove',
      cancelText: 'Cancel',
      variant: 'danger',
    });

    if (!ok) return;

    try {
      await axiosInstance.delete(
        `/carts/${existingCartId}/wishlist/${item.productId}`,
        { useV2: false }
      );

      setWishlistItems(prev =>
        prev.filter(p => p.productId !== item.productId)
      );
    } catch (e) {
      console.error(e);
      showToast(
        apiErrorMessage(e, 'Could not remove item from wishlist.'),
        'error'
      );
    }
  };

  const moveAllWishlistToCart = async (): Promise<void> => {
    if (blockIfReadOnly()) return;
    if (wishlistItems.length === 0) return;

    const existingCartId = ensureCartId();
    if (!existingCartId) return;

    const movableItems = wishlistItems.filter(
      item => item.productQuantity == null || item.productQuantity > 0
    );
    if (movableItems.length === 0) {
      showToast('All wishlist items are currently out of stock.', 'info');
      return;
    }

    const blockedCount = wishlistItems.length - movableItems.length;

    const ok = await confirm({
      title: 'Move all from wishlist',
      message:
        blockedCount > 0
          ? `Move ${movableItems.length} in-stock item(s) to your cart? ${blockedCount} item(s) are out of stock and will remain in your wishlist.`
          : `Move ${movableItems.length} item(s) to your cart?`,
      confirmText: 'Move all',
      cancelText: 'Cancel',
    });
    if (!ok) return;

    const productIds = Array.from(
      new Set(
        movableItems
          .map(item => item.productId)
          .filter((id): id is string => Boolean(id && id.trim()))
      )
    );

    if (productIds.length === 0) {
      showToast('No wishlist items are eligible to move.', 'info');
      return;
    }

    setMovingAll(true);

    try {
      const payload = {
        productIds,
        direction: 'TO_CART' as const,
      };

      const { data } = await axiosInstance.post<CartDetailsModel>(
        `/carts/${existingCartId}/wishlist-transfers`,
        payload,
        { useV2: false }
      );

      const fallback =
        blockedCount > 0
          ? `Moved ${movableItems.length} item(s) to your cart. ${blockedCount} item(s) stayed in your wishlist because they are out of stock.`
          : `Moved ${movableItems.length} item(s) from wishlist to cart.`;
      syncCartState(data, fallback);
    } catch (e) {
      console.error(e);
      showToast(
        apiErrorMessage(e, 'Unexpected error while moving wishlist items.'),
        'error'
      );
    } finally {
      setMovingAll(false);
    }
  };

  const handleCheckoutConfirmation = (): void => {
    if (isStaff) {
      navigate(AppRoutePaths.Unauthorized, {
        state: { message: 'Staff/Admin cannot perform checkout.' },
      });
      return;
    }
    setShowBillingForm(true);
    setIsCheckoutModalOpen(false);
  };

  const handleCheckout = async (billing?: BillingInfo): Promise<void> => {
    if (isStaff) return;

    if (!cartId) {
      setCheckoutMessage('Invalid cart ID');
      return;
    }

    try {
      await axiosInstance.post(
        `/carts/${cartId}/checkout`,
        {},
        { useV2: false }
      );

      const invoiceItems: Invoice[] = cartItems.map(item => ({
        productId: Number(item.productId),
        productName: item.productName,
        productSalePrice: item.productSalePrice,
        quantity: item.quantity || 1,
      }));

      const invoiceItemsForFull: InvoiceItemType[] = invoiceItems.map(i => ({
        productId: i.productId,
        productName: i.productName,
        productSalePrice: i.productSalePrice,
        quantity: i.quantity,
      }));

      // calculate money values in integer cents to avoid floating point errors
      const invoiceSubtotalCents = invoiceItemsForFull.reduce(
        (s, it) => s + Math.round(it.productSalePrice * 100) * it.quantity,
        0
      );
      const invoiceSubtotal = invoiceSubtotalCents / 100;
      const usedBilling = billing ?? billingInfo ?? null;

      const invoiceTaxLines = computeTaxes(
        invoiceSubtotal,
        usedBilling?.province
      ).map(line => ({
        ...line,
        amount: line.amount ?? roundToCents(invoiceSubtotal * line.rate),
      }));
      const invoiceTaxCents = invoiceTaxLines.map(line =>
        Math.round((line.amount ?? 0) * 100)
      );
      const invoiceTaxTotalCents = invoiceTaxCents.reduce(
        (sum, cents) => sum + cents,
        0
      );
      const discountCents = Math.round(effectiveDiscount * 100);
      const invoiceTotalCents =
        invoiceSubtotalCents + invoiceTaxTotalCents - discountCents;
      const invoiceTvq = (invoiceTaxCents[0] ?? 0) / 100;
      const invoiceTvc =
        invoiceTaxCents.slice(1).reduce((sum, cents) => sum + cents, 0) / 100;
      const invoiceTotal = invoiceTotalCents / 100;

      const newInvoice: InvoiceFullType = {
        invoiceId: `${Date.now()}`,
        userId: user?.userId || 'anonymous',
        billing: usedBilling
          ? {
              fullName: usedBilling.fullName,
              email: usedBilling.email,
              address: usedBilling.address,
              city: usedBilling.city,
              province: usedBilling.province,
              postalCode: usedBilling.postalCode,
            }
          : null,
        date: new Date().toISOString(),
        items: invoiceItemsForFull,
        subtotal: invoiceSubtotal,
        tvq: invoiceTvq,
        tvc: invoiceTvc,
        discount: effectiveDiscount,
        total: invoiceTotal,
      };
      setLastInvoice(newInvoice);
      setShowInvoiceModal(true);
      setCheckoutMessage('Checkout successful! Your order is being processed.');
      setCartItems([]);
      setCartItemCount(0);
      setIsCheckoutModalOpen(false);

      // Reset LS so navbar badge = 0 without API
      setCartCountInLS(0);

      // Fetch recent purchases after checkout
      if (customerId) {
        try {
          const { data } = await axiosInstance.get(
            `/customers/${customerId}/cart/recent-purchases`,
            { useV2: false }
          );
          setRecentPurchases(data || []);
        } catch (err) {
          // Optionally handle error, but don't block checkout
        }
      }

      // Fetch recommendation purchases after checkout
      await fetchRecommendationPurchases();
    } catch (error: unknown) {
      if (error && typeof error === 'object' && 'response' in error) {
        const errorData = (
          error as { response?: { data?: { message?: string } } }
        ).response?.data;
        setCheckoutMessage(
          `Checkout failed: ${errorData?.message || 'Unexpected error'}`
        );
      } else {
        setCheckoutMessage('Checkout failed: Unexpected error');
      }
    }
  };

  if (loading)
    return (
      <div
        className="cart-loading-overlay"
        role="status"
        aria-label="Loading cart items"
      >
        <span className="sr-only">Loading cart items...</span>
      </div>
    );
  if (error) return <div className="error">{error}</div>;

  // Helper to filter out duplicate recent purchases by productId
  const uniqueRecentPurchases = recentPurchases.reduce<{
    [id: string]: (typeof recentPurchases)[0];
  }>((acc, item) => {
    if (!acc[item.productId]) {
      acc[item.productId] = item;
    } else {
      // Optionally, sum quantities if duplicate found
      acc[item.productId].quantity += item.quantity;
    }
    return acc;
  }, {});
  const recentPurchasesList = Object.values(uniqueRecentPurchases);

  // Helper to filter out duplicate recommendation purchases by productId
  const uniqueRecommendationPurchases = recommendationPurchases.reduce<{
    [id: string]: (typeof recommendationPurchases)[0];
  }>((acc, item) => {
    if (!acc[item.productId]) {
      acc[item.productId] = item;
    } else {
      acc[item.productId].quantity += item.quantity;
    }
    return acc;
  }, {});
  const recommendationPurchasesList = Object.values(
    uniqueRecommendationPurchases
  );

  const recommendationListClassName = `recommendation-purchases-list recent-purchases-list cart-scroll-strip${
    recommendationPurchasesList.length === 0 ? ' recommendation-empty' : ''
  }`;

  const hasMovableWishlistItems = wishlistItems.some(
    item => item.productQuantity == null || item.productQuantity > 0
  );

  return (
    <div>
      <NavBar />
      <ConfirmModal />
      <div className="UserCart-container">
        <div className="UserCart-checkout-flex">
          <div className="UserCart">
            <div
              className="cart-header"
              style={{
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
              }}
            >
              <div className="cart-badge-container">
                <FaShoppingCart aria-label="Shopping Cart" />
                {cartItemCount > 0 && (
                  <span
                    className="cart-badge"
                    aria-label={`Cart has ${cartItemCount} items`}
                  >
                    {cartItemCount}
                  </span>
                )}
              </div>
              <button
                className="continue-shopping-btn cart-button cart-button--outline"
                onClick={() => navigate('/products')}
                style={{ marginLeft: 'auto' }}
              >
                Continue Shopping
              </button>
            </div>

            <div className="cart-items-container">
              {cartItems.length > 0 ? (
                cartItems.map((item, index) => (
                  <CartItem
                    key={item.productId}
                    item={item}
                    index={index}
                    changeItemQuantity={changeItemQuantity}
                    deleteItem={deleteItem}
                    errorMessage={errorMessages[index]}
                    addToWishlist={addToWishlist}
                    addToCart={() => {}}
                    isInWishlist={false}
                    showNotification={message => showToast(message, 'info')}
                  />
                ))
              ) : (
                <div className="empty-cart-visual">
                  <span
                    className="empty-cart-icon"
                    role="img"
                    aria-label="empty-cart"
                  >
                    🛒
                  </span>
                  <div className="empty-cart-message">
                    Looks like your cart is empty! Why not add something?
                  </div>
                </div>
              )}
            </div>

            <div className="UserCart-buttons">
              {cartItems.length > 0 && (
                <button
                  className="clear-cart-btn cart-button cart-button--danger"
                  onClick={clearCart}
                >
                  Clear Cart
                </button>
              )}
            </div>
          </div>

          {!isStaff && (
            <div className="Checkout-section">
              <div className="voucher-code-section">
                <input
                  type="text"
                  placeholder="Enter voucher code"
                  value={voucherCode}
                  onChange={e => {
                    setVoucherCode(e.target.value);
                    setVoucherError(null);
                  }}
                  className="voucher-input"
                />
                <button
                  onClick={applyVoucherCode}
                  className="apply-voucher-button cart-button cart-button--brand"
                >
                  Apply
                </button>
                {voucherError && (
                  <div className="voucher-error">{voucherError}</div>
                )}
              </div>

              <div className="voucher-code-section" style={{ marginTop: 12 }}>
                {promoPercent != null && (
                  <div
                    className="voucher-hint"
                    style={{
                      marginTop: 6,
                      display: 'flex',
                      gap: 8,
                      alignItems: 'center',
                    }}
                  >
                    <span>Current promo: {promoPercent}%</span>
                    <button
                      type="button"
                      className="cart-button cart-button--outline"
                      onClick={onClearPromo}
                    >
                      Clear
                    </button>
                  </div>
                )}
              </div>

              <div className="CartSummary">
                <h3>Cart Summary</h3>
                <p className="summary-item">
                  Subtotal: {formatPrice(subtotal)}
                </p>
                {resolvedTaxLines.map(line => (
                  <p key={`${line.name}-${line.rate}`} className="summary-item">
                    {line.name} ({formatTaxRate(line.rate)}%):{' '}
                    {formatPrice(line.amount ?? 0)}
                  </p>
                ))}
                {resolvedTaxLines.length > 1 && (
                  <p className="summary-item">
                    Total taxes: {formatPrice(totalTax)}
                  </p>
                )}
                <p className="summary-item">
                  Discount{promoPercent != null ? ` (${promoPercent}%)` : ''}:{' '}
                  {formatPrice(effectiveDiscount)}
                </p>
                <p className="total-price summary-item">
                  Total: {formatPrice(total)}
                </p>
              </div>

              <button
                className="checkout-btn cart-button cart-button--brand cart-button--block cart-button--disabled-muted"
                onClick={handleCheckoutConfirmation}
                disabled={cartItems.length === 0}
              >
                Checkout
              </button>

              {checkoutMessage && (
                <div className="checkout-message">{checkoutMessage}</div>
              )}

              {lastInvoice && (
                <button
                  className="view-receipt-btn cart-button cart-button--brand cart-button--block"
                  onClick={() => setShowInvoiceModal(true)}
                  style={{ marginTop: '1rem' }}
                  aria-label="View your last receipt"
                >
                  View Receipt
                </button>
              )}
            </div>
          )}
        </div>

        <div className="recent-purchases-section cart-panel cart-panel--padded">
          <h2>Recent Purchases</h2>
          <div className="recent-purchases-list cart-scroll-strip">
            {recentPurchasesList.length > 0 ? (
              recentPurchasesList.map(item => (
                <div
                  key={item.productId}
                  className="recent-purchase-card cart-card"
                >
                  <div className="recent-purchase-image-container">
                    <div className="recent-purchase-image">
                      <ImageContainer imageId={item.imageId} />
                    </div>
                  </div>
                  <div className="recent-purchase-name">{item.productName}</div>
                  <div className="recent-purchase-price">
                    ${item.productSalePrice.toFixed(2)}
                  </div>
                  <div className="recent-purchase-qty-row">
                    <label
                      htmlFor={`recent-qty-${item.productId}`}
                      className="recent-purchase-qty-label"
                    >
                      Qty:
                    </label>
                    <input
                      id={`recent-qty-${item.productId}`}
                      type="number"
                      min={1}
                      value={recentPurchaseQuantities[item.productId] || 1}
                      onChange={e =>
                        handleRecentPurchaseQuantityChange(
                          item.productId,
                          Number(e.target.value)
                        )
                      }
                      className="recent-purchase-qty-input"
                    />
                  </div>
                  <button
                    className="purchase-again-btn cart-button cart-button--brand"
                    onClick={() => handlePurchaseAgain(item)}
                  >
                    Purchase Again
                  </button>
                  <div className="recent-purchase-total">
                    Total: $
                    {(
                      item.productSalePrice *
                      (recentPurchaseQuantities[item.productId] || 1)
                    ).toFixed(2)}
                  </div>
                </div>
              ))
            ) : (
              <p>No recent purchases found.</p>
            )}
          </div>
        </div>

        {/* Wishlist */}
        <div className="wishlist-section cart-panel cart-panel--padded">
          {/* Header with Move All button */}
          <div
            className="wishlist-header"
            style={{
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
            }}
          >
            <h2 style={{ margin: 0 }}>Your Wishlist</h2>
            {wishlistItems.length > 0 && (
              <button
                className="cart-button cart-button--accent"
                onClick={moveAllWishlistToCart}
                disabled={isStaff || movingAll || !hasMovableWishlistItems}
                aria-busy={movingAll}
                aria-disabled={isStaff || movingAll || !hasMovableWishlistItems}
                title={
                  isStaff
                    ? 'Read-only: staff/admin cannot move wishlist items'
                    : !hasMovableWishlistItems
                      ? 'All wishlist items are out of stock'
                      : 'Move all wishlist items to cart'
                }
              >
                {movingAll ? 'Moving…' : 'Move All to Cart'}
              </button>
            )}
          </div>

          <div className="Wishlist-items cart-scroll-strip">
            {wishlistItems.length > 0 ? (
              wishlistItems.map(item => {
                const isOutOfStock = item.productQuantity <= 0;
                const wishlistCardClassName = `Wishlist-item cart-card${isOutOfStock ? ' Wishlist-item--out-of-stock' : ''}`;

                return (
                  <div key={item.productId} className={wishlistCardClassName}>
                    {isOutOfStock && (
                      <span
                        className="wishlist-stock-badge"
                        aria-label="No stock left"
                      >
                        No Stock Left
                      </span>
                    )}
                    <div className="recent-purchase-image-container">
                      <div className="recent-purchase-image">
                        <ImageContainer imageId={item.imageId} />
                      </div>
                    </div>
                    <div className="recent-purchase-name">
                      {item.productName}
                    </div>
                    <div className="recent-purchase-price">
                      {formatPrice(item.productSalePrice)}
                    </div>
                    <div className="Wishlist-item-actions">
                      <button
                        className="cart-button cart-button--accent cart-button--block cart-button--strike-disabled"
                        onClick={() => addToCartFunction(item)}
                        disabled={isStaff || isOutOfStock}
                        aria-disabled={isStaff || isOutOfStock}
                      >
                        {isOutOfStock ? 'Out of Stock' : 'Add to Cart'}
                      </button>
                      <button
                        className="cart-button cart-button--danger cart-button--block cart-button--strike-disabled"
                        onClick={() => removeFromWishlist(item)}
                        disabled={isStaff}
                        aria-disabled={isStaff}
                      >
                        Remove
                      </button>
                    </div>
                  </div>
                );
              })
            ) : (
              <p>No products in the wishlist.</p>
            )}
          </div>
        </div>

        <div className="recommendation-purchases-section cart-panel cart-panel--padded">
          <div className="recommendation-header">
            <h2>Your Recommendations</h2>
            <div className="recommendation-subtitle recent-purchases-intro">
              Based on your recent purchases
            </div>
          </div>
          <div className={recommendationListClassName}>
            {recommendationPurchasesList.length > 0 ? (
              recommendationPurchasesList.map(item => (
                <div
                  key={item.productId}
                  className="recommendation-purchase-card cart-card"
                >
                  <div className="recent-purchase-image-container">
                    <div className="recent-purchase-image">
                      <ImageContainer imageId={item.imageId} />
                    </div>
                  </div>
                  <div className="recommendation-product-name recent-purchase-name">
                    {item.productName}
                  </div>
                  <div className="recommendation-product-price recent-purchase-price">
                    ${formatPrice(item.productSalePrice)}
                  </div>
                  <div className="recommendation-qty-row recent-purchase-qty-row">
                    <label
                      htmlFor={`recommendation-qty-${item.productId}`}
                      className="recommendation-qty-label recent-purchase-qty-label"
                    >
                      Qty:
                    </label>
                    <input
                      id={`recommendation-qty-${item.productId}`}
                      type="number"
                      min={1}
                      value={
                        recommendationPurchaseQuantities[item.productId] || 1
                      }
                      onChange={e =>
                        handleRecommendationPurchaseQuantityChange(
                          item.productId,
                          Number(e.target.value)
                        )
                      }
                      className="recommendation-qty-input recent-purchase-qty-input"
                    />
                  </div>
                  <button
                    className="purchase-again-btn cart-button cart-button--brand"
                    onClick={() => handlePurchaseRecommendation(item)}
                  >
                    Purchase Again
                  </button>
                  <div className="recommendation-total recent-purchase-total">
                    Total: $
                    {(
                      item.productSalePrice *
                      (recommendationPurchaseQuantities[item.productId] || 1)
                    ).toFixed(2)}
                  </div>
                </div>
              ))
            ) : (
              <div className="recommendation-empty-state">
                <div className="recommendation-empty-message">
                  No recommendations available yet.
                  <br />
                  Add more products to your cart to get personalized
                  suggestions!
                </div>
                <button
                  className="cta-browse-products-btn cart-button cart-button--brand"
                  onClick={() => navigate('/products')}
                >
                  Browse Products
                </button>
              </div>
            )}
          </div>
        </div>

        {showBillingForm && (
          <div className="modal-backdrop">
            <div className="modal-content">
              <CartBillingForm
                isOpen={true}
                onClose={() => setShowBillingForm(false)}
                onSubmit={async billing => {
                  setBillingInfo(billing);
                  await handleCheckout(billing);
                  setShowBillingForm(false);
                }}
              />
            </div>
          </div>
        )}

        {isCheckoutModalOpen && (
          <div className="checkout-modal">
            <h3>Confirm Checkout</h3>
            <p>Are you sure you want to checkout?</p>
            <button onClick={() => handleCheckout()}>Yes</button>
            <button onClick={() => setIsCheckoutModalOpen(false)}>No</button>
          </div>
        )}
        {showInvoiceModal && lastInvoice && (
          <InvoiceComponent
            invoices={[lastInvoice]}
            index={0}
            onIndexChange={() => {}}
            onClose={() => {
              setShowInvoiceModal(false);
            }}
          />
        )}
      </div>
    </div>
  );
};
export default UserCart;
