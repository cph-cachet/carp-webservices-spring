<#macro emailLayout>
<html>

<head>
  <style>
    @font-face {
      font-family: Museo Sans;
      font-weight: 100;
      src: url(../assets/fonts/museosans-100-webfont.woff);
    }

    @font-face {
      font-family: Museo Sans;
      font-weight: 300;
      src: url(../assets/fonts/museosans-300-webfont.woff);
    }

    @font-face {
      font-family: Museo Sans;
      font-weight: 500;
      src: url(../assets/fonts/museosans-500-webfont.woff);
    }

    @font-face {
      font-family: Museo Sans;
      font-weight: 700;
      src: url(../assets/fonts/museosans-700-webfont.woff);
    }

    @font-face {
      font-family: Museo Sans;
      font-weight: 900;
      src: url(../assets/fonts/museosans-900-webfont.woff);
    }

    header {
      display: flex;
      justify-content: center;
      margin-top: 20px;
    }

    main {
      margin: 1vh;
      background-color: #FCFCFF;

      a {
        border-radius: var(--Radius16, 14px);
        height: 40px auto;
        border: none;
        background: var(--M3-sys-light-primary, #006398);
        color: var(--M3-sys-light-on-primary, #fff);
        text-align: center;
        font-family: Museo Sans;
        font-weight: 700;
        font-size: 14px;
        font-style: normal;
        line-height: 20px;
        letter-spacing: 0.25px;
        padding: 10px 24px;
        text-decoration-line: none;
        white-space: nowrap;
      }
    }

    footer {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      background-color: var(--M3-sys-light-primary, #006398);
      color: var(--M3-sys-light-on-primary, #fff);
      margin-top: 10px;
      padding: 10px;
      border-radius: 5px;

      p {
        margin: 14px;
        font-family: Museo Sans;
        font-weight: 300;
        line-height: 16px;
        text-align: justify;
        text-justify: distribute;
      }

      a {
        color: var(--M3-sys-light-on-primary, #fff);
        text-decoration-line: none;
      }
    }

    img {
      margin-top: 20px;
    }

    p {
      font-family: Museo Sans;
      font-weight: 500;
      font-size: 14px;
      font-style: normal;
      line-height: 20px;
      letter-spacing: 0.25px;
    }

    i {
      font-family: Museo Sans;
      font-weight: 300;
      font-size: 12px;
      font-style: italic;
      line-height: 16px;
      letter-spacing: 0.25px;
      text-decoration-line: none;
    }

    h1 {
      font-family: Museo Sans;
      font-weight: 700;
      font-size: 32px;
      font-style: normal;
      line-height: 36px;
      letter-spacing: 0.25px;
      margin-top: 20px;
      margin-bottom: 20px;
    }

    h2 {
      font-family: Museo Sans;
      font-weight: 700;
      font-size: 24px;
      font-style: normal;
      line-height: 28px;
      letter-spacing: 0.25px;
      margin-top: 10px;
    }

    button {
      border-radius: var(--Radius16, 16px);
      height: 40px auto;
      border: none;
      background: var(--M3-sys-light-primary, #006398);
      color: var(--M3-sys-light-on-primary, #fff);
      text-align: center;
      font-family: Museo Sans;
      font-weight: 600;
      font-size: 0.875rem;
      font-style: normal;
      line-height: 1.25rem;
      letter-spacing: 0.25px;
      padding: 10px 18px 10px 12px;
      cursor: pointer;
    }
  </style>
</head>

<body>
  <header><img src="../assets/images/logo-carp-flat-colored.png" alt="CarpLogo" /></header>
  <main>
    <h1>${kcSanitize(msg("welcomeTitle"))?no_esc}</h1>
    <br>
        <#nested>
    <br>
    <p>Sincerely, <br> Carp Team</p>
  </main>
  <footer>
    <h2>Copenhagen Research Platform</h2>
    <div style="display: flex; width: 100%; flex-direction: row; justify-content:space-evenly; gap: 24px;">
      <a href="https://carp.cachet.dk/"
        style="display: flex; flex-direction: column; justify-content: center; align-items: center;">
        <img src="../assets/images/logo-carp-flat.png" alt="FooterCarpLoo" />
      </a>
      <a href="https://www.dtu.dk/"
        style="display: flex; flex-direction: column; justify-content: center; align-items: center;">
        <img src="../assets/images/footer_DTU.png" alt="DTULogo" />
      </a>
    </div>
    <p>
        ${kcSanitize(msg("footerDescription"))?no_esc}
    </p>
    ${kcSanitize(msg("footerFooterHtml"))?no_esc}
  </footer>
</body>
</html>
</#macro>