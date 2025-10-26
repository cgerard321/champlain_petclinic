import { useCallback, useEffect, useState } from 'react';
import CartBillingForm, { BillingInfo } from './CartBillingForm';
import InvoiceComponent, {
  InvoiceFull as InvoiceFullType,
  InvoiceItem as InvoiceItemType,
} from './Invoice';
import { useNavigate, useParams } from 'react-router-dom';
import { useCart } from '@/context/CartContext';
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
import { applyPromo } from '@/shared/api/cart';
import {
  IsAdmin,
  IsInventoryManager,
  IsReceptionist,
  IsVet,
  useUser,
} from '@/context/UserContext';
import {
  computeTaxes,
  averageCanadianCombinedTaxRate,
  roundToCents,
} from '../utils/taxUtils';
import { AppRoutePaths } from '@/shared/models/path.routes';
import { getProductByProductId } from '@/features/products/api/getProductByProductId';
import {
  bumpCartCountInLS,
  notifyCartChanged,
  setCartCountInLS,
  setCartIdInLS,
} from '../api/cartEvent';
import { useConfirmModal } from '@/shared/hooks/useConfirmModal';
interface ProductAPIResponse {
  productId: number;
  imageId: string;
  productName: string;
  productDescription: string;
  productSalePrice: number;
  averageRating: number;
  quantityInCart: number;
  productQuantity: number;
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
  const { syncAfterAddToCart } = useCart();
  const { confirm, ConfirmModal } = useConfirmModal();

  const [cartItems, setCartItems] = useState<ProductModel[]>([]);
  const [wishlistItems, setWishlistItems] = useState<ProductModel[]>([]);

  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [errorMessages, setErrorMessages] = useState<Record<number, string>>(
    {}
  );
  const [notificationMessage, setNotificationMessage] = useState<string | null>(
    null
  );

  const [checkoutMessage, setCheckoutMessage] = useState<string | null>(null);
  const [showInvoiceModal, setShowInvoiceModal] = useState<boolean>(false);
  const [lastInvoice, setLastInvoice] = useState<InvoiceFullType | null>(null);
  const [cartItemCount, setCartItemCount] = useState<number>(0);
  const [isCheckoutModalOpen, setIsCheckoutModalOpen] =
    useState<boolean>(false);
  const [showBillingForm, setShowBillingForm] = useState<boolean>(false);
  const [billingInfo, setBillingInfo] = useState<BillingInfo | null>(null);

  const [wishlistUpdated, setWishlistUpdated] = useState(false);
  const [voucherCode, setVoucherCode] = useState<string>('');
  const [voucherError, setVoucherError] = useState<string | null>(null);

  const [movingAll, setMovingAll] = useState<boolean>(false);

  const [promoPercent, setPromoPercent] = useState<number | null>(null);

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
    if (!cartId) return;

    const quantity = Math.max(1, recentPurchaseQuantities[item.productId] || 1);

    try {
      // Concurrently add items to cart
      const addPromises = Array.from({ length: quantity }, () =>
        axiosInstance.post(
          `/carts/${cartId}/${item.productId}`,
          {},
          { useV2: false }
        )
      );
      await Promise.all(addPromises);

      setNotificationMessage(
        `${item.productName} (x${quantity}) added to cart!`
      );
      notifyCartChanged();

      // Fetch updated cart and update state
      const { data } = await axiosInstance.get(`/carts/${cartId}`, {
        useV2: false,
      });
      if (Array.isArray(data.products)) {
        const products: ProductModel[] = data.products.map(
          (p: ProductAPIResponse) => ({
            productId: p.productId,
            imageId: p.imageId,
            productName: p.productName,
            productDescription: p.productDescription,
            productSalePrice: p.productSalePrice,
            averageRating: p.averageRating,
            quantity: p.quantityInCart || 1,
            productQuantity: p.productQuantity,
          })
        );
        setCartItems(products);
        const updatedCount = products.reduce(
          (acc, p) => acc + (p.quantity || 0),
          0
        );
        setCartCountInLS(updatedCount);
      }
    } catch (err: unknown) {
      const msg = getErrorMessage(err, {
        defaultMessage: `Failed to add ${item.productName} to cart.`,
      });
      setNotificationMessage(msg);
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
    if (!cartId) return;

    const quantity = Math.max(
      1,
      recommendationPurchaseQuantities[item.productId] || 1
    );

    try {
      for (let i = 0; i < quantity; i += 1) {
        await axiosInstance.post(
          `/carts/${cartId}/${item.productId}`,
          {},
          { useV2: false }
        );
      }
      setNotificationMessage(
        `${item.productName} (x${quantity}) added to cart!`
      );
      notifyCartChanged();
      // Fetch updated cart and update state
      const { data } = await axiosInstance.get(`/carts/${cartId}`, {
        useV2: false,
      });
      if (Array.isArray(data.products)) {
        const products: ProductModel[] = data.products.map(
          (p: ProductAPIResponse) => ({
            productId: p.productId,
            imageId: p.imageId,
            productName: p.productName,
            productDescription: p.productDescription,
            productSalePrice: p.productSalePrice,
            averageRating: p.averageRating,
            quantity: p.quantityInCart || 1,
            productQuantity: p.productQuantity,
          })
        );
        setCartItems(products);
        await syncAfterAddToCart();
      }
    } catch (err: unknown) {
      const msg = getErrorMessage(err, {
        defaultMessage: `Failed to add ${item.productName} to cart.`,
      });
      setNotificationMessage(msg);
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
  const effectiveDiscount = promoDiscount;

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
        const { data } = await axiosInstance.get(`/carts/${cartId}`, {
          useV2: false,
        });

        if (!Array.isArray(data.products)) {
          throw new Error('Invalid data format: products should be an array');
        }

        const products: ProductModel[] = data.products.map(
          (p: ProductAPIResponse) => ({
            productId: p.productId,
            imageId: p.imageId,
            productName: p.productName,
            productDescription: p.productDescription,
            productSalePrice: p.productSalePrice,
            averageRating: p.averageRating,
            quantity: p.quantityInCart || 1,
            productQuantity: p.productQuantity,
          })
        );

        setCartItems(products);
        setCartIdInLS(cartId);
        await syncAfterAddToCart();
        const enrichedWishlist = await Promise.all(
          (data.wishListProducts || []).map(async (item: ProductModel) => {
            const fullProduct = await getProductByProductId(item.productId);
            return {
              ...fullProduct,
              quantity: item.quantity ?? 1,
            };
          })
        );
        setWishlistItems(enrichedWishlist);

        if (typeof data.promoPercent === 'number') {
          setPromoPercent(data.promoPercent);
        } else {
          setPromoPercent(null);
        }
      } catch (err: unknown) {
        console.error(err);
        setError('Failed to fetch cart items');
      } finally {
        setLoading(false);
        if (wishlistUpdated) setWishlistUpdated(false);
      }
    };

    fetchCartItems();
  }, [cartId, wishlistUpdated, syncAfterAddToCart]);

  useEffect(() => {
    updateCartItemCount();
  }, [cartItems, updateCartItemCount]);

  // Fetch recent purchases
  useEffect(() => {
    if (!cartId) return;
    const fetchRecentPurchases = async (): Promise<void> => {
      try {
        const { data } = await axiosInstance.get(
          `/carts/${cartId}/recent-purchases`,
          { useV2: false }
        );
        setRecentPurchases(data || []);
      } catch (err) {
        setRecentPurchases([]);
      }
    };
    fetchRecentPurchases();
  }, [cartId]);

  // Fetch recommendation purchases
  // Reusable function to fetch recommendation purchases
  const fetchRecommendationPurchases = useCallback(async (): Promise<void> => {
    if (!cartId) return;
    try {
      const { data } = await axiosInstance.get(
        `/carts/${cartId}/recommendation-purchases`,
        { useV2: false }
      );
      setRecommendationPurchases(data || []);
    } catch (err) {
      setRecommendationPurchases([]);
    }
  }, [cartId]);

  useEffect(() => {
    fetchRecommendationPurchases();
  }, [cartId, fetchRecommendationPurchases]);

  const applyVoucherCode = async (): Promise<void> => {
    if (!cartId) {
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

      const updated = await applyPromo(cartId, percent);

      setVoucherError(null);
      setNotificationMessage(
        `Promo applied${updated?.promoPercent != null ? `: ${updated.promoPercent}%` : `: ${percent}%`}`
      );

      setPromoPercent(updated?.promoPercent ?? percent);

      try {
        const refreshed = await axiosInstance.get(`/carts/${cartId}`, {
          useV2: false,
        });
        if (typeof refreshed.data?.promoPercent === 'number') {
          setPromoPercent(refreshed.data.promoPercent);
        }
      } catch {
        /* ignore */
      }
    } catch (err: unknown) {
      const msg = getErrorMessage(err, {
        defaultMessage: 'Promo code invalid or expired.',
      });
      setVoucherError(msg);
    }
  };

  const blockIfReadOnly = useCallback((): boolean => {
    if (isStaff) {
      setNotificationMessage(
        'Read-only mode: staff/admin cannot modify carts.'
      );
      return true;
    }
    return false;
  }, [isStaff, setNotificationMessage]);

  const changeItemQuantity = useCallback(
    async (
      event: React.ChangeEvent<HTMLInputElement>,
      index: number
    ): Promise<void> => {
      if (blockIfReadOnly()) return;

      const newQuantity = Math.max(1, Number(event.target.value));
      const item = cartItems[index];
      const prevQty = item.quantity || 1;
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
        const { data } = await axiosInstance.put(
          `/carts/${cartId}/products/${item.productId}`,
          { quantity: newQuantity },
          { useV2: false }
        );

        if (data && data.message) {
          setErrorMessages(prevErrors => ({
            ...prevErrors,
            [index]: data.message || 'Failed to update quantity',
          }));

          if (
            typeof data.message === 'string' &&
            data.message.includes('moved to your wishlist')
          ) {
            setCartItems(prevItems =>
              prevItems.filter((_, idx) => idx !== index)
            );
            setWishlistItems(prevItems => [...prevItems, item]);
            setNotificationMessage(data.message);
            bumpCartCountInLS(-prevQty);
            notifyCartChanged(); // left cart
            return;
          }
        } else {
          setCartItems(prev => {
            const next = [...prev];
            next[index] = { ...next[index], quantity: newQuantity };
            return next;
          });
          setNotificationMessage('Item quantity updated successfully.');
          bumpCartCountInLS(newQuantity - prevQty);
          notifyCartChanged(); // qty changed
        }
      } catch (err: unknown) {
        const msg = getErrorMessage(err, {
          defaultMessage: 'Failed to update quantity',
        });
        setErrorMessages(prev => ({
          ...prev,
          [index]: msg,
        }));
      }
    },
    [cartItems, cartId, blockIfReadOnly]
  );

  const onClearPromo = async (): Promise<void> => {
    if (!cartId) return;
    try {
      await applyPromo(cartId, 0);
      setPromoPercent(null);
      setNotificationMessage('Promo removed.');
    } catch {
      setNotificationMessage('Failed to remove promo.');
    }
  };

  const deleteItem = useCallback(
    async (productId: string, indexToDelete: number): Promise<void> => {
      if (blockIfReadOnly()) return;
      if (!cartId) return;

      const ok = await confirm({
        title: 'Remove item',
        message: 'Remove this item from your cart?',
        confirmText: 'Remove',
        cancelText: 'Cancel',
        variant: 'danger',
      });
      if (!ok) return;

      try {
        await axiosInstance.delete(`/carts/${cartId}/${productId}`, {
          useV2: false,
        });

        setCartItems(prev => {
          const removedQty = prev[indexToDelete]?.quantity || 1;
          setNotificationMessage('Item removed from cart.');
          bumpCartCountInLS(-removedQty);
          return prev.filter((_, idx) => idx !== indexToDelete);
        });
        notifyCartChanged(); // item removed
      } catch (error: unknown) {
        const msg = getErrorMessage(error, {
          defaultMessage: 'Failed to delete item.',
        });
        setNotificationMessage(msg);
      }
    },
    [cartId, blockIfReadOnly, confirm]
  );

  const clearCart = async (): Promise<void> => {
    if (blockIfReadOnly()) return;

    if (!cartId) {
      setNotificationMessage('Invalid cart ID');

      return;
    }

    const ok = await confirm({
      title: 'Clear cart',
      message: 'Are you sure you want to clear the cart?',
      confirmText: 'Clear cart',
      cancelText: 'Cancel',
      variant: 'danger',
    });
    if (!ok) return;

    try {
      await axiosInstance.delete(`/carts/${cartId}/clear`, { useV2: false });
      setCartItems([]);
      setCartItemCount(0);
      setCartCountInLS(0);
      notifyCartChanged();
      setNotificationMessage('Cart has been cleared.');
    } catch (error: unknown) {
      const msg = getErrorMessage(error, {
        defaultMessage: 'Failed to clear cart.',
      });
      setNotificationMessage(msg);
    }
  };

  const addToWishlist = async (item: ProductModel): Promise<void> => {
    if (blockIfReadOnly()) return;

    try {
      const productId = item.productId;

      const { data } = await axiosInstance.put(
        `/carts/${cartId}/wishlist/${productId}/toWishList`,
        {
          productId: item.productId,
          imageId: item.imageId,
          productName: item.productName,
          productSalePrice: item.productSalePrice,
        },
        { useV2: false }
      );

      if (data && data.message) {
        setNotificationMessage(data.message);
      } else {
        setNotificationMessage(
          `${item.productName} has been added to your wishlist!`
        );
      }

      setWishlistItems(prevItems => [...prevItems, item]);
      setWishlistUpdated(true);

      // Decrement LS count since item left the cart
      bumpCartCountInLS(-(item.quantity || 1));

      //notify navbar (item moved out of cart)
      notifyCartChanged();
    } catch (error: unknown) {
      const msg = getErrorMessage(error, {
        defaultMessage: 'Failed to add item to wishlist.',
      });
      setNotificationMessage(msg);
    }
  };

  const addToCartFunction = async (item: ProductModel): Promise<void> => {
    if (blockIfReadOnly()) return;

    if (item.productQuantity <= 0) {
      setNotificationMessage(
        `${item.productName} is out of stock and cannot be added to the cart.`
      );
      return;
    }
    try {
      const productId = item.productId;

      const { data } = await axiosInstance.put(
        `/carts/${cartId}/wishlist/${productId}/toCart`,
        {
          productId: item.productId,
          imageId: item.imageId,
          productName: item.productName,
          productSalePrice: item.productSalePrice,
        },
        { useV2: false }
      );

      if (data && data.message) {
        setNotificationMessage(data.message);
      } else {
        setNotificationMessage(
          `${item.productName} has been added to your cart!`
        );
      }

      setCartItems(prevItems => [...prevItems, item]);
      setWishlistItems(prevItems =>
        prevItems.filter(product => product.productId !== item.productId)
      );
      setWishlistUpdated(true);

      // Item entered the cart, increment LS count
      bumpCartCountInLS(item.quantity || 1);

      //notify navbar (item moved into cart)
      notifyCartChanged();
    } catch (error: unknown) {
      const msg = getErrorMessage(error, {
        defaultMessage: 'Failed to add item to cart.',
      });
      setNotificationMessage(msg);
    }
  };

  const removeFromWishlist = async (item: ProductModel): Promise<void> => {
    if (blockIfReadOnly()) return;
    if (!cartId) return;

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
        `/carts/${cartId}/wishlist/${item.productId}`,
        { useV2: false }
      );

      setWishlistItems(prev =>
        prev.filter(p => p.productId !== item.productId)
      );
    } catch (e: unknown) {
      const msg = getErrorMessage(e, {
        defaultMessage: 'Could not remove item from wishlist.',
      });
      setNotificationMessage(msg);
    }
  };

  const moveAllWishlistToCart = async (): Promise<void> => {
    if (blockIfReadOnly()) return;
    if (!cartId || wishlistItems.length === 0) return;

    const ok = await confirm({
      title: 'Move all from wishlist',
      message: `Move ${wishlistItems.length} item(s) to your cart?`,
      confirmText: 'Move all',
      cancelText: 'Cancel',
    });
    if (!ok) return;

    setMovingAll(true);
    setNotificationMessage(null);

    try {
      const res = await axiosInstance.post(
        `/carts/${cartId}/wishlist/moveAll`,
        {},
        { useV2: false, validateStatus: () => true }
      );

      if (res.status >= 200 && res.status < 300) {
        setWishlistUpdated(true);
        notifyCartChanged();
        setNotificationMessage(null);
      } else {
        const msg =
          (res.data &&
            (res.data.message || res.data.error || res.data.title)) ||
          `Move All failed (${res.status})`;
        setNotificationMessage(msg);
      }
    } catch (e: unknown) {
      const msg = getErrorMessage(e, {
        defaultMessage: 'Unexpected error while moving wishlist items.',
      });
      setNotificationMessage(msg);
    }
  };

  const { user } = useUser();

  // For cart summary we always show a single estimated tax using the
  // average Canadian combined tax rate to keep the summary snappy.
  const avgRate = averageCanadianCombinedTaxRate();
  const estimatedTax = roundToCents(discountedSubtotal * avgRate);
  const total = discountedSubtotal + estimatedTax;

  const handleCheckoutConfirmation = (): void => {
    if (isStaff) {
      navigate(AppRoutePaths.Unauthorized, {
        state: { message: 'Staff/Admin cannot perform checkout.' },
      });
      return;
    }
    // Opening billing form implies the user wants to provide billing details
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
      // determine province for invoice tax calculation
      // Prefer billing/provided province, otherwise use user's province if available.
      function hasProvince(obj: unknown): obj is { province?: string } {
        if (typeof obj !== 'object' || obj === null) return false;
        // eslint-disable-next-line @typescript-eslint/ban-ts-comment
        // @ts-ignore - narrow below
        const val = (obj as { [k: string]: unknown })['province'];
        return typeof val === 'string' || typeof val === 'undefined';
      }

      const usedProvince =
        (billing ?? billingInfo)?.province ||
        (hasProvince(user) ? user.province : undefined);
      const invoiceTaxLines = computeTaxes(invoiceSubtotal, usedProvince);
      const invoiceTaxCents = invoiceTaxLines.reduce((s, t) => {
        const amount = t.amount ?? roundToCents(invoiceSubtotal * t.rate);
        return s + Math.round(amount * 100);
      }, 0);
      const discountCents = Math.round(effectiveDiscount * 100);
      const invoiceTotal =
        (invoiceSubtotalCents + invoiceTaxCents - discountCents) / 100;

      const usedBilling = billing ?? billingInfo ?? null;

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
        // legacy fields: map by tax line name for backward compatibility
        // Older code expects 'tvq' (provincial) and 'tvc' (federal). Map common
        // names from our tax lines. NOTE: PROVINCE_TAX_MAP uses 'PST' for QC
        // (not 'QST'/'TVQ'), so check for 'PST' here.
        tvq:
          invoiceTaxLines.find(t => ['PST', 'TVQ'].includes(t.name))?.amount ??
          0,
        // For tvc prefer HST (single combined) then GST
        tvc:
          invoiceTaxLines.find(t => t.name === 'HST')?.amount ??
          invoiceTaxLines.find(t => t.name === 'GST')?.amount ??
          0,
        discount: effectiveDiscount,
        total: invoiceTotal,
      };
      setLastInvoice(newInvoice);
      setShowInvoiceModal(true);
      setCheckoutMessage('Checkout successful! Your order is being processed.');
      // keep cart summary showing average estimated tax
      setCartItems([]);
      setCartItemCount(0);
      setIsCheckoutModalOpen(false);

      // Reset LS so navbar badge = 0 without API
      setCartCountInLS(0);

      // notify navbar (cart emptied)
      notifyCartChanged();

      // Fetch recent purchases after checkout
      try {
        const { data } = await axiosInstance.get(
          `/carts/${cartId}/recent-purchases`,
          { useV2: false }
        );
        setRecentPurchases(data || []);
      } catch (err) {
        // Optionally handle error, but don't block checkout
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

  return (
    <div>
      <NavBar />
      <ConfirmModal />
      <div className="UserCart-container">
        {notificationMessage && (
          <div className="notification-message">
            {notificationMessage}
            <button
              className="close-notification"
              onClick={() => setNotificationMessage(null)}
              aria-label="Close notification"
            >
              &times;
            </button>
          </div>
        )}

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
                    showNotification={setNotificationMessage}
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
                {/* Show estimated taxes (English names like PST, GST, HST).
                    If subtotal is zero (e.g., after checkout) show a single $0.00 line and do not render breakdown.
                    Otherwise, if there's exactly one Estimated Taxes line (unknown province), render a single line without percentage.
                */}
                {discountedSubtotal <= 0 ? (
                  <p className="summary-item">
                    Estimated Taxes: {formatPrice(0)}
                  </p>
                ) : (
                  // Always show a single estimated taxes line using the average rate
                  <p className="summary-item">
                    Estimated Taxes: {formatPrice(estimatedTax)}
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
                disabled={isStaff || movingAll}
                aria-busy={movingAll}
                aria-disabled={isStaff || movingAll}
                title={
                  isStaff
                    ? 'Read-only: staff/admin cannot move wishlist items'
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
                return (
                  <div key={item.productId} className="Wishlist-item cart-card">
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
