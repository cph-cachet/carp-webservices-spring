//This is to show that you can create stories for pages that you haven't overloaded.

import { ComponentMeta, ComponentStory } from '@storybook/react';
import { createPageStory } from '../createPageStory';

const { PageStory } = createPageStory({
  pageId: 'info.ftl',
});

export default {
  title: 'login/Info',
  component: PageStory,
} as ComponentMeta<typeof PageStory>;

export const Default: ComponentStory<typeof PageStory> = () => (
  <PageStory
    kcContext={{
      messageHeader: 'Account update required',
      message: {
        summary: 'Your administrator has requested you to.',
        type: 'info',
      },
      requiredActions: ['UPDATE_PASSWORD'],
    }}
  />
);
