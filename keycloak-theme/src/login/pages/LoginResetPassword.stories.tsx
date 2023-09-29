//This is to show that you can create stories for pages that you haven't overloaded.

import { ComponentMeta, ComponentStory } from '@storybook/react';
import { createPageStory } from '../createPageStory';

const { PageStory } = createPageStory({
  pageId: 'login-reset-password.ftl',
});

export default {
  title: 'login/LoginResetPassword',
  component: PageStory,
} as ComponentMeta<typeof PageStory>;

export const Default: ComponentStory<typeof PageStory> = () => <PageStory />;

