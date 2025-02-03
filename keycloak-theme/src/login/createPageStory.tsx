import type { DeepPartial } from "keycloakify/tools/DeepPartial";
import { getKcContext, type KcContext } from "./kcContext";
import KcApp from "./KcApp";

const createPageStory = <PageId extends KcContext["pageId"]>(params: {
  pageId: PageId;
}) => {
  const { pageId } = params;

  const PageStory = (props: {
    kcContext?: DeepPartial<Extract<KcContext, { pageId: PageId }>>;
  }) => {
    const { kcContext: context } = props;
    const { kcContext } = getKcContext({
      mockPageId: pageId,
      storyPartialKcContext: context,
    });

    return <KcApp kcContext={kcContext} />;
  };

  return { PageStory };
};

export default createPageStory;
