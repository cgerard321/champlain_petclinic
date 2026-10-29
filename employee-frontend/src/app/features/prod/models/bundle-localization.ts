const BUNDLE_NAMES: Readonly<Record<string, () => string>> = {
  'Dog Bundle': () => $localize`:@@products.bundles.names.dog:Ensemble pour chiens`,
  'Fish Bundle': () => $localize`:@@products.bundles.names.fish:Ensemble pour poissons`,
  'Accessory Bundle': () => $localize`:@@products.bundles.names.accessory:Ensemble d'accessoires`,
  'Saddle Special': () => $localize`:@@products.bundles.names.saddle:Spécial selle`,
  'Aquarium Starter Kit': () =>
    $localize`:@@products.bundles.names.aquarium:Ensemble de départ pour aquarium`,
  'New Kitten Bundle': () =>
    $localize`:@@products.bundles.names.kitten:Ensemble pour nouveau chaton`,
  'Dog Walk & Care Bundle': () =>
    $localize`:@@products.bundles.names.dogCare:Ensemble de promenade et de soins pour chiens`,
  'Senior Pet Comfort Bundle': () =>
    $localize`:@@products.bundles.names.senior:Ensemble confort pour animaux âgés`,
  'Small Animal & Bird Bundle': () =>
    $localize`:@@products.bundles.names.smallAnimal:Ensemble pour petits animaux et oiseaux`,
  'Home Vet Care Bundle': () =>
    $localize`:@@products.bundles.names.homeCare:Ensemble de soins vétérinaires à domicile`,
  'Ultimate Pet Owner Bundle': () =>
    $localize`:@@products.bundles.names.ultimate:Ensemble ultime pour propriétaires d'animaux`,
};

const BUNDLE_DESCRIPTIONS: Readonly<Record<string, () => string>> = {
  'Dog Food & Flea Collar': () =>
    $localize`:@@products.bundles.descriptions.dog:Nourriture pour chiens et collier antipuces`,
  'Cat Litter & Fish Tank Heater': () =>
    $localize`:@@products.bundles.descriptions.fish:Litière pour chats et chauffe-eau pour aquarium`,
  'All Accessories': () =>
    $localize`:@@products.bundles.descriptions.accessory:Tous les accessoires`,
  'Horse Saddle on sale': () =>
    $localize`:@@products.bundles.descriptions.saddle:Selle pour cheval en solde`,
  'Filter, heater & fish flakes for a new tank': () =>
    $localize`:@@products.bundles.descriptions.aquarium:Filtre, chauffe-eau et flocons pour poissons pour un nouvel aquarium`,
  'Everything a new kitten needs at home': () =>
    $localize`:@@products.bundles.descriptions.kitten:Tout ce dont un nouveau chaton a besoin à la maison`,
  'Food, walking gear and grooming for dogs': () =>
    $localize`:@@products.bundles.descriptions.dogCare:Nourriture, accessoires de promenade et toilettage pour chiens`,
  'Comfort, health and monitoring for aging pets': () =>
    $localize`:@@products.bundles.descriptions.senior:Confort, santé et suivi pour les animaux vieillissants`,
  'Housing, food and accessories for rabbits and birds': () =>
    $localize`:@@products.bundles.descriptions.smallAnimal:Habitat, nourriture et accessoires pour lapins et oiseaux`,
  'Treatments and tools for at-home pet health care': () =>
    $localize`:@@products.bundles.descriptions.homeCare:Traitements et outils pour les soins de santé des animaux à domicile`,
  'Our biggest bundle: food, gear and equipment for every pet': () =>
    $localize`:@@products.bundles.descriptions.ultimate:Notre plus grand ensemble : nourriture, accessoires et équipement pour tous les animaux`,
};

export function localizedBundleName(name: string): string {
  return BUNDLE_NAMES[name]?.() ?? name;
}

export function localizedBundleDescription(description: string): string {
  return BUNDLE_DESCRIPTIONS[description]?.() ?? description;
}
