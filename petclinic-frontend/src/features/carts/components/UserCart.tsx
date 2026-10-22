import { useCallback, useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useProductText } from '../utils/useProductText';
import { useToast } from '@/shared/components/toast/ToastProvider';
import CartBillingForm, { BillingInfo } from './CartBillingForm';
import InvoiceComponent, {
  InvoiceFull as InvoiceFullType,
  InvoiceItem as InvoiceItemType,
} from './Invoice';
import { generatePath, useNavigate, useParams } from 'react-router-dom';
import CartItem from './CartItem';
import { ProductModel } from '../models/ProductModel';
import './cart-shared.css';
import './UserCart.css';
import { NavBar } from '@/layouts/AppNavBar';
import { FaShoppingCart, FaHeart, FaRegHeart } from 'react-icons/fa';
import { useAddToWishlist } from '../api/addToWishlistFromProducts';
import { useRemoveFromWishlistByIcon } from '../api/removeProductFromWishlist';
import ImageContainer from '@/features/products/components/ImageContainer';
import axiosInstance from '@/shared/api/axiosInstance';
import getErrorMessage from '@/shared/api/getErrorMessage';
import { formatPrice } from '../utils/formatPrice';
import {
  applyPromo,
  clearPromo,
  updateShippingMethod,
  type CartDetailsModel,
  type ShippingMethod,
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
import {
  computeTaxes,
  formatTaxRate,
  roundToCents,
  taxLabelKey,
} from '../utils/taxUtils';
import { useConfirmModal } from '@/shared/hooks/useConfirmModal';
import { getCustomerOrders } from '../api/getCustomerOrders';
import type { OrderModel } from '../models/OrderModel';
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
  productId: string;
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
  const { t } = useTranslation('carts');
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
  const [orders, setOrders] = useState<OrderModel[]>([]);
  const [ordersLoading, setOrdersLoading] = useState(true);
  const [ordersError, setOrdersError] = useState<string | null>(null);

  const [promoPercent, setPromoPercent] = useState<number | null>(null);
  const [shippingMethod, setShippingMethod] =
    useState<ShippingMethod>('PICKUP');
  const [shippingCost, setShippingCost] = useState<number>(0);

  const fetchOrders = useCallback(async (): Promise<void> => {
    if (!customerId) {
      setOrders([]);
      setOrdersLoading(false);
      return;
    }

    setOrdersLoading(true);
    setOrdersError(null);

    try {
      const data = await getCustomerOrders(customerId);
      setOrders(Array.isArray(data) ? data : []);
    } catch (error) {
      console.error('Failed to fetch orders:', error);
      setOrdersError(t('orders.loadFailed'));
    } finally {
      setOrdersLoading(false);
    }
  }, [customerId, t]);

  useEffect(() => {
    void fetchOrders();
  }, [fetchOrders]);

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

  const redirectToProductPageOnClick = (productId: string): void => {
    navigate(
      generatePath(AppRoutePaths.ProductDetails, {
        productId,
      })
    );
  };

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
      if (getStatusCode(err) === 404) return t('errors.cartNotFound');
      return getErrorMessage(err, { defaultMessage });
    },
    [getStatusCode, t]
  );

  const ensureCartId = useCallback((): string | undefined => {
    if (!cartId) {
      const message = 'errors.invalidCartId';
      setError(message);
      showToast(t(message), 'error');
      return undefined;
    }
    return cartId;
  }, [cartId, setError, showToast, t]);

  const syncCartState = useCallback(
    (
      cartData: CartDetailsModel | null | undefined,
      fallbackMessage?: string,
      options?: { preferFallback?: boolean }
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
      setShippingMethod(cartData.shippingMethod ?? 'PICKUP');
      setShippingCost(
        typeof cartData.shippingCost === 'number' ? cartData.shippingCost : 0
      );

      // sse are in english so prefer the localized fallback when asked
      const serverMessage =
        !options?.preferFallback && typeof cartData.message === 'string'
          ? cartData.message
          : undefined;
      emitCartMessage(serverMessage, fallbackMessage);
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

      syncCartState(
        data,
        t('toasts.purchaseAdded', { name: localizedName(item), quantity })
      );
    } catch (err: unknown) {
      showToast(
        apiErrorMessage(
          err,
          t('errors.addProductFailed', { name: localizedName(item) })
        ),
        'error'
      );
    }
  };

  const { addToWishlist: addRecommendationToWishlist } = useAddToWishlist();
  const { removeFromWishlistByIcon } = useRemoveFromWishlistByIcon();

  const isInWishlist = (productId: string): boolean =>
    wishlistItems.some(p => p.productId === productId);

  const toggleRecommendationWishlist = async (
    productId: string
  ): Promise<void> => {
    let success;
    if (isInWishlist(productId)) {
      success = await removeFromWishlistByIcon(productId);
    } else {
      success = await addRecommendationToWishlist(productId, 1);
    }

    if (!success) {
      showToast(t('errors.updateWishlistFailed'), 'error');
      return;
    }

    const { data } = await axiosInstance.get<CartDetailsModel>(
      `/carts/${cartId}`,
      { useV2: false }
    );
    syncCartState(data);
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

  const { localizedName } = useProductText([
    ...cartItems,
    ...wishlistItems,
    ...recentPurchases,
    ...recommendationPurchases,
    ...orders.flatMap(order => order.products),
  ]);

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
      syncCartState(
        data,
        t('toasts.purchaseAdded', { name: localizedName(item), quantity })
      );
    } catch (err: unknown) {
      showToast(
        apiErrorMessage(
          err,
          t('errors.addProductFailed', { name: localizedName(item) })
        ),
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
  const effectiveShippingCost = cartItems.length > 0 ? shippingCost : 0;
  const taxableSubtotal = roundToCents(
    discountedSubtotal + effectiveShippingCost
  );
  const resolvedTaxLines = computeTaxes(
    taxableSubtotal,
    billingInfo?.province
  ).map(line => ({
    ...line,
    amount: line.amount ?? roundToCents(taxableSubtotal * line.rate),
  }));
  const totalTax = roundToCents(
    resolvedTaxLines.reduce((acc, line) => acc + (line.amount ?? 0), 0)
  );

  const effectiveDiscount = promoDiscount;

  const total = roundToCents(taxableSubtotal + totalTax);

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
        setError('errors.invalidCartId');
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
        setError('errors.fetchFailed');
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
      setVoucherError('errors.invalidCartId');
      return;
    }
    if (!voucherCode.trim()) {
      setVoucherError('errors.promoRequired');
      return;
    }

    try {
      const { data } = await axiosInstance.get(
        `/carts/promos/validate/${encodeURIComponent(voucherCode.trim())}`,
        { useV2: false, handleLocally: true }
      );

      const percent: number = Number(data?.discount);
      if (Number.isNaN(percent) || percent < 0 || percent > 100) {
        setVoucherError('errors.invalidDiscount');
        return;
      }

      const updated = await applyPromo(existingCartId, percent);

      setVoucherError(null);
      const appliedMessage = t('toasts.promoApplied', {
        percent: updated?.promoPercent ?? percent,
      });
      syncCartState(updated, appliedMessage);
    } catch (err) {
      console.error('Error validating promo code:', err);
      setVoucherError('errors.promoInvalid');
    }
  };

  const blockIfReadOnly = useCallback((): boolean => {
    if (isStaff) {
      showToast(t('toasts.readOnly'), 'info');
      return true;
    }
    return false;
  }, [isStaff, showToast, t]);

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
        let stockLimitMessage = t('errors.stockLimit', {
          stock: item.productQuantity,
        });
        if (item.productQuantity === 1) {
          stockLimitMessage = t('errors.stockLimitOne');
        }
        setErrorMessages(prevErrors => ({
          ...prevErrors,
          [index]: stockLimitMessage,
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
            [index]: data.message ?? t('errors.updateQuantityFailed'),
          }));
          syncCartState(data, data.message);
          return;
        }

        setErrorMessages(prevErrors => {
          const next = { ...prevErrors };
          delete next[index];
          return next;
        });
        syncCartState(data, t('toasts.quantityUpdated'));
      } catch (err) {
        console.error('Error updating quantity:', err);
        setErrorMessages(prev => ({
          ...prev,
          [index]: t('errors.updateQuantityFailed'),
        }));
      }
    },
    [cartItems, blockIfReadOnly, ensureCartId, syncCartState, t]
  );

  const onClearPromo = async (): Promise<void> => {
    const existingCartId = ensureCartId();
    if (!existingCartId) return;
    try {
      await clearPromo(existingCartId);
      setPromoPercent(null);
      showToast(t('toasts.promoRemoved'), 'success');
    } catch (err: unknown) {
      showToast(apiErrorMessage(err, t('errors.removePromoFailed')), 'error');
    }
  };

  const onShippingMethodChange = async (
    event: React.ChangeEvent<HTMLSelectElement>
  ): Promise<void> => {
    if (blockIfReadOnly()) return;
    const existingCartId = ensureCartId();
    if (!existingCartId) return;

    const nextShippingMethod = event.target.value as ShippingMethod;
    try {
      const updatedCart = await updateShippingMethod(
        existingCartId,
        nextShippingMethod
      );
      syncCartState(updatedCart, t('shipping.updated'));
    } catch (err: unknown) {
      showToast(apiErrorMessage(err, t('shipping.updateFailed')), 'error');
    }
  };

  const deleteItem = useCallback(
    async (productId: string, indexToDelete: number): Promise<void> => {
      if (blockIfReadOnly()) return;
      const existingCartId = ensureCartId();
      if (!existingCartId) return;

      const ok = await confirm({
        title: t('confirm.removeItemTitle'),
        message: t('confirm.removeItemMessage'),
        confirmText: t('buttons.remove'),
        cancelText: t('buttons.cancel'),
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
        showToast(t('toasts.itemRemoved'), 'success');
      } catch (error) {
        console.error('Error deleting item: ', error);
        showToast(
          apiErrorMessage(error, t('errors.deleteItemFailed')),
          'error'
        );
      }
    },
    [apiErrorMessage, blockIfReadOnly, confirm, ensureCartId, showToast, t]
  );

  const clearCart = async (): Promise<void> => {
    if (blockIfReadOnly()) return;
    const existingCartId = ensureCartId();
    if (!existingCartId) return;

    const ok = await confirm({
      title: t('confirm.clearCartTitle'),
      message: t('confirm.clearCartMessage'),
      confirmText: t('confirm.clearCartTitle'),
      cancelText: t('buttons.cancel'),
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
      showToast(t('toasts.cartCleared'), 'success');
    } catch (error) {
      console.error('Error clearing cart:', error);
      showToast(apiErrorMessage(error, t('errors.clearCartFailed')), 'error');
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

      const fallback = t('toasts.addedToWishlist', {
        name: localizedName(item),
      });
      syncCartState(data, fallback, { preferFallback: true });
    } catch (error: unknown) {
      console.error('Error adding to wishlist:', error);
      showToast(
        apiErrorMessage(error, t('errors.addToWishlistFailed')),
        'error'
      );
    }
  };

  const addToCartFunction = async (item: ProductModel): Promise<void> => {
    if (blockIfReadOnly()) return;

    const existingCartId = ensureCartId();
    if (!existingCartId) return;

    if (item.productQuantity <= 0) {
      showToast(t('toasts.outOfStock', { name: localizedName(item) }), 'info');
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

      const fallback = t('toasts.addedToCart', { name: localizedName(item) });
      syncCartState(data, fallback, { preferFallback: true });
    } catch (error: unknown) {
      console.error('Error adding to cart:', error);
      showToast(apiErrorMessage(error, t('errors.addToCartFailed')), 'error');
    }
  };

  const removeFromWishlist = async (item: ProductModel): Promise<void> => {
    if (blockIfReadOnly()) return;

    const existingCartId = ensureCartId();
    if (!existingCartId) return;

    const ok = await confirm({
      title: t('confirm.removeWishlistTitle'),
      message: t('confirm.removeWishlistMessage', {
        name: localizedName(item),
      }),
      confirmText: t('buttons.remove'),
      cancelText: t('buttons.cancel'),
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
        apiErrorMessage(e, t('errors.removeFromWishlistFailed')),
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
      showToast(t('toasts.wishlistAllOutOfStock'), 'info');
      return;
    }

    const blockedCount = wishlistItems.length - movableItems.length;
    const counts = { movable: movableItems.length, blocked: blockedCount };

    const ok = await confirm({
      title: t('confirm.moveAllTitle'),
      message:
        blockedCount > 0
          ? t('confirm.moveAllMessageBlocked', counts)
          : t('confirm.moveAllMessage', counts),
      confirmText: t('confirm.moveAllConfirm'),
      cancelText: t('buttons.cancel'),
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
      showToast(t('toasts.noEligible'), 'info');
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
          ? t('toasts.movedBlocked', counts)
          : t('toasts.moved', counts);
      syncCartState(data, fallback, { preferFallback: true });
    } catch (e) {
      console.error(e);
      showToast(apiErrorMessage(e, t('errors.moveFailed')), 'error');
    } finally {
      setMovingAll(false);
    }
  };

  const handleViewOrderReceipt = (order: OrderModel): void => {
    const discount = roundToCents(
      order.subtotal * ((order.promoPercent ?? 0) / 100)
    );

    const orderBilling = {
      customerName: order.customerName,
      email: order.email,
      address: order.address,
      city: order.city,
      province: order.province,
      postalCode: order.postalCode,
    };
    const invoice: InvoiceFullType = {
      invoiceId: order.invoiceId || order.orderId,
      userId: order.customerId,
      billing: orderBilling,
      date: order.orderDate,
      items: order.products.map(item => ({
        productId: item.productId,
        productName: item.productName,
        productSalePrice: item.productSalePrice,
        quantity: item.quantityInCart,
      })),
      subtotal: order.subtotal,
      taxableSubtotal: roundToCents(order.total - order.tvq - order.tvc),
      tvq: order.tvq,
      tvc: order.tvc,
      discount,
      shippingMethod: order.shippingMethod ?? 'PICKUP',
      shippingCost: order.shippingCost ?? 0,
      total: order.total,
    };

    setLastInvoice(invoice);
    setShowInvoiceModal(true);
  };

  const handleCheckoutConfirmation = (): void => {
    if (isStaff) {
      navigate(AppRoutePaths.Unauthorized, {
        state: { message: t('toasts.staffCheckout') },
      });
      return;
    }
    setShowBillingForm(true);
    setIsCheckoutModalOpen(false);
  };

  const handleCheckout = async (billing?: BillingInfo): Promise<void> => {
    if (isStaff) return;

    if (!cartId) {
      setCheckoutMessage(t('errors.invalidCartId'));
      return;
    }
    try {
      const usedBilling = billing ?? billingInfo;

      if (!usedBilling) {
        setCheckoutMessage('Billing information is required.');
        return;
      }
      const checkoutBilling = {
        customerName: usedBilling.customerName,
        email: usedBilling.email,
        address: usedBilling.address,
        city: usedBilling.city,
        province: usedBilling.province,
        postalCode: usedBilling.postalCode,
      };

      await axiosInstance.post(`/carts/${cartId}/checkout`, checkoutBilling, {
        useV2: false,
      });

      const invoiceItems: Invoice[] = cartItems.map(item => ({
        productId: item.productId,
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
      const shippingCents = Math.round(effectiveShippingCost * 100);
      const discountCents = Math.round(effectiveDiscount * 100);
      const invoiceTaxableSubtotalCents = Math.max(
        0,
        invoiceSubtotalCents - discountCents + shippingCents
      );
      const invoiceTaxableSubtotal = invoiceTaxableSubtotalCents / 100;

      const invoiceTaxLines = computeTaxes(
        invoiceTaxableSubtotal,
        usedBilling?.province
      ).map(line => ({
        ...line,
        amount: line.amount ?? roundToCents(invoiceTaxableSubtotal * line.rate),
      }));
      const invoiceTaxCents = invoiceTaxLines.map(line =>
        Math.round((line.amount ?? 0) * 100)
      );
      const invoiceTaxTotalCents = invoiceTaxCents.reduce(
        (sum, cents) => sum + cents,
        0
      );
      const invoiceTotalCents =
        invoiceTaxableSubtotalCents + invoiceTaxTotalCents;
      const invoiceTvq = (invoiceTaxCents[0] ?? 0) / 100;
      const invoiceTvc =
        invoiceTaxCents.slice(1).reduce((sum, cents) => sum + cents, 0) / 100;
      const invoiceTotal = invoiceTotalCents / 100;

      const newInvoice: InvoiceFullType = {
        invoiceId: `${Date.now()}`,
        userId: user?.userId || 'anonymous',
        billing: usedBilling
          ? {
              customerName: usedBilling.customerName,
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
        taxableSubtotal: invoiceTaxableSubtotal,
        tvq: invoiceTvq,
        tvc: invoiceTvc,
        discount: effectiveDiscount,
        shippingMethod,
        shippingCost: effectiveShippingCost,
        total: invoiceTotal,
      };
      setLastInvoice(newInvoice);
      setShowInvoiceModal(true);
      setCheckoutMessage(t('toasts.checkoutSuccess'));
      setCartItems([]);
      setCartItemCount(0);
      setShippingMethod('PICKUP');
      setShippingCost(0);
      setIsCheckoutModalOpen(false);

      // Reset LS so navbar badge = 0 without API
      setCartCountInLS(0);

      // Refresh cart data to retrieve the newly created order

      await fetchOrders();

      try {
        const { data } = await axiosInstance.get<CartDetailsModel>(
          `/carts/${cartId}`,
          { useV2: false }
        );

        syncCartState(data);
      } catch (err) {
        console.error('Failed to refresh orders after checkout:', err);
      }

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
          t('errors.checkoutFailed', {
            message: errorData?.message || t('errors.unexpected'),
          })
        );
      } else {
        setCheckoutMessage(
          t('errors.checkoutFailed', { message: t('errors.unexpected') })
        );
      }
    }
  };

  if (loading)
    return (
      <div
        className="cart-loading-overlay"
        role="status"
        aria-label={t('cart.loading')}
      >
        <span className="sr-only">{t('cart.loading')}...</span>
      </div>
    );
  if (error) return <div className="error">{t(error)}</div>;

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

  const displayAddToCartOrPurchaseAgain = (productId: string): string => {
    return recentPurchasesList.some(item => item.productId === productId)
      ? t('buttons.purchaseAgain')
      : t('buttons.addToCart');
  };

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
                <FaShoppingCart aria-label={t('cart.shoppingCart')} />
                {cartItemCount > 0 && (
                  <span
                    className="cart-badge"
                    aria-label={t('cart.itemCount', { count: cartItemCount })}
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
                {t('buttons.continueShopping')}
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
                    aria-label={t('cart.emptyLabel')}
                  >
                    🛒
                  </span>
                  <div className="empty-cart-message">
                    {t('cart.emptyMessage')}
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
                  {t('buttons.clearCart')}
                </button>
              )}
            </div>
          </div>

          {!isStaff && (
            <div className="Checkout-section">
              <div className="voucher-code-section">
                <input
                  type="text"
                  placeholder={t('cart.voucherPlaceholder')}
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
                  {t('buttons.apply')}
                </button>
                {voucherError && (
                  <div className="voucher-error">{t(voucherError)}</div>
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
                    <span>
                      {t('cart.currentPromo', { percent: promoPercent })}
                    </span>
                    <button
                      type="button"
                      className="cart-button cart-button--outline"
                      onClick={onClearPromo}
                    >
                      {t('buttons.clear')}
                    </button>
                  </div>
                )}
              </div>

              {cartItems.length > 0 && (
                <div className="voucher-code-section" style={{ marginTop: 12 }}>
                  <label htmlFor="shipping-method">{t('shipping.label')}</label>
                  <select
                    id="shipping-method"
                    value={shippingMethod}
                    onChange={onShippingMethodChange}
                    className="shipping-method-select"
                  >
                    <option value="PICKUP">
                      {t('shipping.pickup', { price: formatPrice(0) })}
                    </option>
                    <option value="STANDARD">
                      {t('shipping.standard', { price: formatPrice(9.99) })}
                    </option>
                    <option value="EXPRESS">
                      {t('shipping.express', { price: formatPrice(19.99) })}
                    </option>
                  </select>
                </div>
              )}

              <div className="CartSummary">
                <h3>{t('summary.title')}</h3>
                <p className="summary-item">
                  {t('summary.itemsSubtotal')} {formatPrice(subtotal)}
                </p>
                <p className="summary-item">
                  {promoPercent != null
                    ? t('summary.discountWithPercent', {
                        percent: promoPercent,
                      })
                    : t('summary.discount')}{' '}
                  {formatPrice(effectiveDiscount)}
                </p>
                <p className="summary-item">
                  {t('summary.shipping')} {formatPrice(effectiveShippingCost)}
                </p>
                <p className="summary-item">
                  {t('summary.subtotal')} {formatPrice(taxableSubtotal)}
                </p>
                {resolvedTaxLines.map(line => (
                  <p key={`${line.name}-${line.rate}`} className="summary-item">
                    {t(
                      `taxes.${taxLabelKey(line.name, billingInfo?.province)}`,
                      {
                        defaultValue: line.name,
                      }
                    )}{' '}
                    ({formatTaxRate(line.rate)}%):{' '}
                    {formatPrice(line.amount ?? 0)}
                  </p>
                ))}
                {resolvedTaxLines.length > 1 && (
                  <p className="summary-item">
                    {t('summary.totalTaxes')} {formatPrice(totalTax)}
                  </p>
                )}
                <p className="total-price summary-item">
                  {t('summary.total')} {formatPrice(total)}
                </p>
              </div>

              <button
                className="checkout-btn cart-button cart-button--brand cart-button--block cart-button--disabled-muted"
                onClick={handleCheckoutConfirmation}
                disabled={cartItems.length === 0}
              >
                {t('buttons.checkout')}
              </button>

              {checkoutMessage && (
                <div className="checkout-message">{checkoutMessage}</div>
              )}

              {orders.length > 0 && (
                <button
                  type="button"
                  className="view-receipt-btn cart-button cart-button--brand cart-button--block"
                  onClick={() => {
                    const latestOrder = orders.reduce((latest, order) =>
                      new Date(order.orderDate).getTime() >
                      new Date(latest.orderDate).getTime()
                        ? order
                        : latest
                    );

                    handleViewOrderReceipt(latestOrder);
                  }}
                  style={{ marginTop: '1rem' }}
                  aria-label={t('cart.receiptLabel')}
                >
                  {t('buttons.viewReceipt')}
                </button>
              )}
            </div>
          )}
        </div>

        {/* Previous Orders */}
        <div className="orders-section cart-panel cart-panel--padded">
          <h2>{t('orders.title')}</h2>

          {ordersLoading ? (
            <p>{t('orders.loading')}</p>
          ) : ordersError ? (
            <p className="error">{ordersError}</p>
          ) : orders.length === 0 ? (
            <p>{t('orders.empty')}</p>
          ) : (
            <div className="orders-list">
              {orders.map(order => (
                <div key={order.orderId} className="order-card cart-card">
                  <div className="order-header">
                    <h3>{t('orders.orderNumber', { id: order.orderId })}</h3>

                    <p>
                      {t('orders.placedOn')}{' '}
                      {new Date(order.orderDate).toLocaleDateString()}
                    </p>
                  </div>

                  <div className="order-products">
                    {order.products.map(item => (
                      <div key={item.productId} className="order-product">
                        <div
                          className="recent-purchase-image"
                          onClick={() =>
                            redirectToProductPageOnClick(item.productId)
                          }
                        >
                          <ImageContainer
                            productId={item.productId}
                            imageId={item.imageId}
                          />
                        </div>

                        <div className="order-product-details">
                          <h4>{localizedName(item)}</h4>

                          <p>
                            {t('orders.quantity')} {item.quantityInCart}
                          </p>

                          <p>
                            {t('orders.each', {
                              price: formatPrice(item.productSalePrice),
                            })}
                          </p>
                        </div>
                      </div>
                    ))}
                  </div>

                  <div className="order-footer">
                    <button
                      type="button"
                      className="view-receipt-btn cart-button cart-button--brand cart-button--block"
                      onClick={() => handleViewOrderReceipt(order)}
                      style={{ marginTop: '1rem' }}
                      aria-label={t('cart.receiptLabel')}
                    >
                      {t('buttons.viewReceipt')}
                    </button>
                    <span>
                      {t('orders.subtotal')} {formatPrice(order.subtotal)}
                    </span>
                    <span>
                      {order.shippingMethod
                        ? t('orders.shippingWithMethod', {
                            method: order.shippingMethod,
                          })
                        : t('orders.shipping')}{' '}
                      {formatPrice(order.shippingCost ?? 0)}
                    </span>
                    <span>
                      {t('orders.tvq')} {formatPrice(order.tvq)}
                    </span>
                    <span>
                      {t('orders.tvc')} {formatPrice(order.tvc)}
                    </span>

                    {order.promoPercent != null && (
                      <span>
                        {t('orders.promo', { percent: order.promoPercent })}
                      </span>
                    )}

                    <strong>
                      {t('orders.total')} {formatPrice(order.total)}
                    </strong>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Recent purchases*/}
        <div className="recent-purchases-section cart-panel cart-panel--padded">
          <h2>{t('recent.title')}</h2>
          <div className="recent-purchases-list cart-scroll-strip">
            {recentPurchasesList.length > 0 ? (
              recentPurchasesList.map(item => (
                <div
                  key={item.productId}
                  className="recent-purchase-card cart-card"
                >
                  <div className="recent-purchase-image-container">
                    <div
                      onClick={() =>
                        redirectToProductPageOnClick(item.productId)
                      }
                      className="recent-purchase-image"
                    >
                      <ImageContainer
                        productId={item.productId}
                        imageId={item.imageId}
                      />
                    </div>
                  </div>
                  <div className="recent-purchase-name">
                    {localizedName(item)}
                  </div>
                  <div className="recent-purchase-price">
                    ${item.productSalePrice.toFixed(2)}
                  </div>
                  <div className="recent-purchase-qty-row">
                    <label
                      htmlFor={`recent-qty-${item.productId}`}
                      className="recent-purchase-qty-label"
                    >
                      {t('recent.quantity')}
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
                    {t('buttons.purchaseAgain')}
                  </button>
                  <div className="recent-purchase-total">
                    {t('summary.total')} $
                    {(
                      item.productSalePrice *
                      (recentPurchaseQuantities[item.productId] || 1)
                    ).toFixed(2)}
                  </div>
                </div>
              ))
            ) : (
              <p>{t('recent.empty')}</p>
            )}
          </div>
        </div>

        {/* Wishlist */}
        <div className="wishlist-section cart-panel cart-panel--padded">
          {/* Header with Add All button */}
          <div
            className="wishlist-header"
            style={{
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
            }}
          >
            <h2 style={{ margin: 0 }}>{t('wishlist.title')}</h2>
            {wishlistItems.length > 0 && (
              <button
                className="cart-button cart-button--accent"
                onClick={moveAllWishlistToCart}
                disabled={isStaff || movingAll || !hasMovableWishlistItems}
                aria-busy={movingAll}
                aria-disabled={isStaff || movingAll || !hasMovableWishlistItems}
                title={
                  isStaff
                    ? t('wishlist.staffReadOnly')
                    : !hasMovableWishlistItems
                      ? t('wishlist.allOutOfStock')
                      : t('wishlist.moveAllTitle')
                }
              >
                {movingAll ? t('wishlist.moving') : t('buttons.moveAllToCart')}
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
                    <div className="recent-purchase-image-container">
                      <div
                        onClick={() =>
                          redirectToProductPageOnClick(item.productId)
                        }
                        className="recent-purchase-image"
                      >
                        <ImageContainer
                          productId={item.productId}
                          imageId={item.imageId}
                        />
                      </div>
                    </div>
                    <div className="recent-purchase-name">
                      {localizedName(item)}
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
                        {isOutOfStock
                          ? t('buttons.outOfStock')
                          : t('buttons.addToCart')}
                      </button>
                      <button
                        className="cart-button cart-button--danger cart-button--block cart-button--strike-disabled"
                        onClick={() => removeFromWishlist(item)}
                        disabled={isStaff}
                        aria-disabled={isStaff}
                      >
                        {t('buttons.remove')}
                      </button>
                    </div>
                  </div>
                );
              })
            ) : (
              <p>{t('wishlist.empty')}</p>
            )}
          </div>
        </div>

        <div className="recommendation-purchases-section cart-panel cart-panel--padded">
          <div className="recommendation-header">
            <h2>{t('recommendations.title')}</h2>
            <div className="recommendation-subtitle recent-purchases-intro">
              {t('recommendations.subtitle')}
            </div>
          </div>
          <div className={recommendationListClassName}>
            {recommendationPurchasesList.length > 0 ? (
              recommendationPurchasesList.map(item => (
                <div
                  key={item.productId}
                  className="recommendation-purchase-card cart-card"
                >
                  <button
                    className="wishlist-heart-btn"
                    title="Add to Wishlist"
                    onClick={() => toggleRecommendationWishlist(item.productId)}
                  >
                    {isInWishlist(item.productId) && (
                      <FaHeart style={{ color: '#e11d48' }} />
                    )}
                    {!isInWishlist(item.productId) && (
                      <FaRegHeart style={{ color: '#000' }} />
                    )}
                  </button>
                  <div className="recent-purchase-image-container">
                    <div
                      onClick={() =>
                        redirectToProductPageOnClick(item.productId)
                      }
                      className="recent-purchase-image"
                    >
                      <ImageContainer
                        productId={item.productId}
                        imageId={item.imageId}
                      />
                    </div>
                  </div>
                  <div className="recommendation-product-name recent-purchase-name">
                    {localizedName(item)}
                  </div>
                  <div className="recommendation-product-price recent-purchase-price">
                    {formatPrice(item.productSalePrice)}
                  </div>
                  <div className="recommendation-qty-row recent-purchase-qty-row">
                    <label
                      htmlFor={`recommendation-qty-${item.productId}`}
                      className="recommendation-qty-label recent-purchase-qty-label"
                    >
                      {t('recent.quantity')}
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
                    {displayAddToCartOrPurchaseAgain(item.productId)}
                  </button>
                  <div className="recommendation-total recent-purchase-total">
                    {t('summary.total')} $
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
                  {t('recommendations.empty')}
                  <br />
                  {t('recommendations.emptyHint')}
                </div>
                <button
                  className="cta-browse-products-btn cart-button cart-button--brand"
                  onClick={() => navigate('/products')}
                >
                  {t('buttons.browseProducts')}
                </button>
              </div>
            )}
          </div>
        </div>

        {showBillingForm && (
          <CartBillingForm
            isOpen={true}
            onClose={() => setShowBillingForm(false)}
            onSubmit={async billing => {
              setBillingInfo(billing);
              await handleCheckout(billing);
              setShowBillingForm(false);
            }}
          />
        )}

        {isCheckoutModalOpen && (
          <div className="checkout-modal">
            <h3>{t('checkoutModal.title')}</h3>
            <p>{t('checkoutModal.message')}</p>
            <button onClick={() => handleCheckout()}>{t('buttons.yes')}</button>
            <button onClick={() => setIsCheckoutModalOpen(false)}>
              {t('buttons.no')}
            </button>
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
