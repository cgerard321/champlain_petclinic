import { useEffect, useMemo, useState } from 'react';
import axiosInstance from '@/shared/api/axiosInstance';
import { getAllVets } from '@/features/veterinarians/api/getAllVets';
import { VetResponseModel } from '@/features/veterinarians/models/VetResponseModel';

const VET_TAGS = [
  'General Check-ups',
  'Vaccinations',
  'Dental Care',
  'Surgery',
  'Emergency Services',
  'Pet Grooming',
  'Nutritional Advice',
];

// Literally a random pick function in TS
function pick<T>(arr: T[], n: number): T[] {
  const a = [...arr];

  for (let i = a.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [a[i], a[j]] = [a[j], a[i]];
  }

  return a.slice(0, n);
}

// eslint-disable-next-line @typescript-eslint/explicit-function-return-type
export default function useFeaturedVets(n = 3) {
  const [vets, setVets] = useState<VetResponseModel[]>([]);
  const [photos, setPhotos] = useState<Record<string, string>>({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let mounted = true;
    const urls: string[] = [];

    // eslint-disable-next-line @typescript-eslint/explicit-function-return-type
    const fetchPhoto = async (vetId: string) => {
      try {
        const res = await axiosInstance.get(`/vets/${vetId}/photo`, {
          useV2: true,
          responseType: 'blob',
          headers: { Accept: 'image/*' },
        });

        const url = URL.createObjectURL(res.data as Blob);

        urls.push(url);

        if (mounted) setPhotos(p => ({ ...p, [vetId]: url }));
      } catch {
        if (mounted)
          setPhotos(p => ({ ...p, [vetId]: '/images/vet_default.jpg' }));
      }
    };

    // Get all vets and pick randomly from vet list
    (async () => {
      try {
        const all = await getAllVets();

        const featured = pick(all, n);

        if (!mounted) return;

        setVets(featured);

        await Promise.all(featured.map(v => fetchPhoto(v.vetId)));
      } catch (e) {
        if (mounted) setError('Failed to fetch vets');
      } finally {
        if (mounted) setLoading(false);
      }
    })();

    return () => {
      mounted = false;
      urls.forEach(URL.revokeObjectURL);
    };
  }, [n]);

  const tagsByVet = useMemo(
    () => Object.fromEntries(vets.map(v => [v.vetId, pick(VET_TAGS, 2)])),
    [vets]
  );

  return { vets, photos, tagsByVet, loading, error };
}
