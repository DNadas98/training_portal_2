import useLocaleContext from "./useLocaleContext.tsx";
import {formatISO, parse} from "date-fns";
import {getDateFnsLocale} from "../utils/getDateFnsLocale.ts";

export default function useLocalizedSubmittedDate() {
  const {locale} = useLocaleContext()

  const toSubmittedDate = (dateString: string): string => {
    const dateFormat = () => {
      switch (locale.toString()) {
        case "huHU":
          return "yyyy. MM. dd. HH:mm";
        case "enGB":
          return "dd/MM/yyyy HH:mm";
        default:
          return "yyyy. MM. dd. HH:mm";
      }
    }

    const date = parse(dateString, dateFormat(), new Date(), {locale: getDateFnsLocale(locale)});

    return formatISO(date);
  }
  return toSubmittedDate;
}
