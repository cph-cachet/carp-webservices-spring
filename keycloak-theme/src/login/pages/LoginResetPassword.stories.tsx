// This is to show that you can create stories for pages that you haven't overloaded.

import { Meta, StoryObj } from "@storybook/react";
import createPageStory from "../createPageStory";

const { PageStory } = createPageStory({
  pageId: "login-reset-password.ftl",
});

const meta = {
  title: "login/LoginResetPassword",
  component: PageStory,
} satisfies Meta<typeof PageStory>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {
  render: () => <PageStory />,
};

export const Danish: Story = {
  render: () => (
    <PageStory kcContext={{ locale: { currentLanguageTag: "da" } }} />
  ),
};
