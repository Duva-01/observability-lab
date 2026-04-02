local skip = false
local seen_title = false

function Header(el)
  if el.level == 1 and not seen_title then
    seen_title = true
    return {}
  end

  local text = pandoc.utils.stringify(el)

  if el.level == 2 and (text == "Portada" or text == "Índice detallado") then
    skip = true
    return {}
  end

  if skip and el.level == 2 then
    skip = false
    return el
  end

  if skip then
    return {}
  end

  return el
end

function Para(el)
  if skip then
    return {}
  end
  return el
end

function BlockQuote(el)
  if skip then
    return {}
  end
  return el
end

function BulletList(el)
  if skip then
    return {}
  end
  return el
end

function OrderedList(el)
  if skip then
    return {}
  end
  return el
end

function HorizontalRule(el)
  if skip then
    return {}
  end
  return el
end
