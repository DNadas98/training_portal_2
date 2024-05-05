interface ISiteConfig {
  readonly siteName: string,
  readonly siteURL: string,
  readonly sourceCodeUrl: string,
  readonly defaultLocale: string,
  readonly adminInfo: adminInfo,
  readonly developerInfo: developerInfo
}

interface adminInfo {
  readonly name_en: string,
  readonly name_hu: string,
  readonly mail: string,
}

interface developerInfo {
  readonly name_en: string,
  readonly name_hu: string,
  readonly mail: string,
  readonly portfolioUrl: string,
  readonly portfolioTitle: string
}

const siteConfig: ISiteConfig = {
  siteName: "vizsgasor.hu",
  siteURL: "https://vizsgasor.hu",
  sourceCodeUrl: "https://github.com/DNadas98/training_portal_2",
  defaultLocale: "huHU",
  adminInfo: {
    name_en: "Ferenc Nádas",
    name_hu: "Nádas Ferenc",
    mail: "vizsgasor@fnadas.net"
  },
  developerInfo: {
    name_en: "Dániel Nádas",
    name_hu: "Nádas Dániel",
    mail: "daniel.nadas@dnadas.net",
    portfolioUrl: "https://dnadas.net",
    portfolioTitle: "dnadas.net"
  }
};
export default siteConfig;
