import type { DeepPartial } from "keycloakify/tools/DeepPartial";
import { getKcContext, type KcContext } from "./kcContext";
import KcApp from "./KcApp";

export function createPageStory<PageId extends KcContext["pageId"]>(params: {
  pageId: PageId;
}) {

  const { pageId } = params;

  const PageStory = (params: { kcContext?: DeepPartial<Extract<KcContext, { pageId: PageId }>>; }) => {

    const { kcContext } = getKcContext({
      mockPageId: pageId,
      storyPartialKcContext: params.kcContext
    });

    return <KcApp kcContext={kcContext} />;

  }

  return { PageStory };

}
