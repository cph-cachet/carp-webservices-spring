import { styled } from "../../utils/theme";

export const DefaultLogo = styled("a")({
  boxSizing: "border-box",
  display: "flex",
  height: 60,
  margin: "0",
  padding: "10px 24px !important",
  position: "relative",
  textAlign: "left",
  textDecoration: "none !important",

  "& img": {
    display: "inline-block",
    height: 45,
    marginRight: 5,
  },
});

export const FlatLogo = styled("a")({
  boxSizing: "border-box",
  display: "flex",
  margin: "5px auto",
  position: "relative",
  textAlign: "left",
  textDecoration: "none !important",

  "& img": {
    height: 35,
  },
});

export const FlatColoredLogo = styled("a")({
  boxSizing: "border-box",
  display: "flex",
  alignItems: "center",
  height: 60,
  margin: "0",
  position: "relative",
  textAlign: "left",
  textDecoration: "none !important",

  "& img": {
    display: "inline-block",
    height: 28,
    marginRight: 5,
  },
});

export const FlatWhiteLogo = styled("a")({
  boxSizing: "border-box",
  "& img": {
    height: 28,
  },
});
