import { ComponentStory, ComponentMeta } from '@storybook/react';
import { createPageStory } from "../createPageStory";

const { PageStory } = createPageStory({
    pageId: "login.ftl"
});

export default {
    title: "login/Login",
    component: PageStory,
} as ComponentMeta<typeof PageStory>;

export const Default: ComponentStory<typeof PageStory> = () => <PageStory />;

