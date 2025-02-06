// This is to show that you can create stories for pages that you haven't overloaded.

import { Meta, StoryObj } from "@storybook/react";
import createPageStory from "../createPageStory";

const { PageStory } = createPageStory({
  pageId: "info.ftl",
});

const meta = {
  title: "login/Info",
  component: PageStory,
} satisfies Meta<typeof PageStory>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {
  render: () => (
    <PageStory
      kcContext={{
        messageHeader: "Account update required",
        message: {
          summary: "Your administrator has requested you to",
          type: "info",
        },
        requiredActions: ["UPDATE_PASSWORD", "VERIFY_EMAIL"],
      }}
    />
  ),
};
